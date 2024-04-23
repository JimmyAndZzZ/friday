package com.jimmy.friday.framework.transaction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Striped;
import com.jimmy.friday.boot.core.transaction.TransactionFacts;
import com.jimmy.friday.boot.enums.AckTypeEnum;
import com.jimmy.friday.boot.enums.TransactionStatusEnum;
import com.jimmy.friday.boot.enums.TransactionTypeEnum;
import com.jimmy.friday.boot.exception.ConnectionException;
import com.jimmy.friday.boot.message.transaction.TransactionCompensation;
import com.jimmy.friday.boot.message.transaction.TransactionRegister;
import com.jimmy.friday.boot.message.transaction.TransactionSubmit;
import com.jimmy.friday.boot.message.transaction.TransactionSubmitAck;
import com.jimmy.friday.boot.other.ConfigConstants;
import com.jimmy.friday.framework.base.TransactionConnectionProxy;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.support.TransmitSupport;
import com.jimmy.friday.framework.utils.TransactionConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Slf4j
public class TransactionSession {

    private final Striped<Lock> stripes = Striped.lock(16);

    private final Set<String> process = ConcurrentHashMap.newKeySet();

    private final Map<Long, CountDownLatch> waitAckMap = Maps.newConcurrentMap();

    private final Map<Long, AckTypeEnum> confirmAckMap = Maps.newConcurrentMap();

    private final Map<TransactionTypeEnum, TransactionConnectionProxy> proxyMap = new HashMap<>();

    private final Map<String, TransactionStatusEnum> notifyTransactionStatus = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, List<TransactionFacts>> transactionHolder = new ConcurrentHashMap<>();

    private final ExecutorService executorService = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private final ConfigLoad configLoad;

    private final TransmitSupport transmitSupport;

    private final ApplicationContext applicationContext;

    public TransactionSession(ConfigLoad configLoad, TransmitSupport transmitSupport, ApplicationContext applicationContext) {
        this.configLoad = configLoad;
        this.transmitSupport = transmitSupport;
        this.applicationContext = applicationContext;
    }

    public void initialize() {
        Map<String, TransactionConnectionProxy> beansOfType = this.applicationContext.getBeansOfType(TransactionConnectionProxy.class);
        beansOfType.values().forEach(bean -> proxyMap.put(bean.type(), bean));
    }

    public void notify(Long traceId, AckTypeEnum ackTypeEnum) {
        CountDownLatch countDownLatch = waitAckMap.remove(traceId);
        if (countDownLatch != null) {
            confirmAckMap.put(traceId, ackTypeEnum);
            countDownLatch.countDown();
        }
    }

    public Connection getConnection(TransactionTypeEnum transactionTypeEnum, Connection connection, String id, String dsName) {
        return this.proxyMap.get(transactionTypeEnum).getConnection(connection, id, dsName);
    }

    public TransactionConnectionProxy getProxy(TransactionTypeEnum transactionTypeEnum) {
        return this.proxyMap.get(transactionTypeEnum);
    }

    public void submit(String transactionId, TransactionStatusEnum transactionStatus) {
        Lock lock = stripes.get(transactionId);
        lock.lock();
        try {
            String path = configLoad.get(ConfigConstants.TRANSACTION_POINT_ROOT_PATH, TransactionConstants.DEFAULT_FILE_PATH);

            if (FileUtil.exist(path + transactionId + TransactionConstants.FILE_SUFFIX_DONE)) {
                log.error("transactionId:{}已处理", transactionId);
                return;
            }

            if (FileUtil.exist(path + transactionId + TransactionConstants.FILE_SUFFIX_STATE)) {
                return;
            }
            //创建事务文件
            FileUtil.writeString(transactionStatus.getState(), path + transactionId + TransactionConstants.FILE_SUFFIX_STATE, StandardCharsets.UTF_8);

            long traceId = IdUtil.getSnowflake(1, 1).nextId();

            CountDownLatch countDownLatch = new CountDownLatch(1);
            waitAckMap.put(traceId, countDownLatch);

            TransactionSubmit transactionSubmit = new TransactionSubmit();
            transactionSubmit.setTraceId(traceId);
            transactionSubmit.setId(transactionId);
            transactionSubmit.setTransactionStatus(transactionStatus);
            this.transmitSupport.send(transactionSubmit);

            if (!this.await(transactionId, traceId, countDownLatch)) {
                this.rollback(transactionId);
            }
        } catch (ConnectionException connectionException) {
            this.rollback(transactionId);
        } catch (Exception e) {
            log.error("事务提交失败", e);
        } finally {
            lock.unlock();
        }
    }

    public void collectTransaction(TransactionFacts transactionFacts) {
        String transactionId = transactionFacts.getTransactionId();

        Lock lock = stripes.get(transactionId);
        lock.lock();
        try {
            //服务端已回调
            TransactionStatusEnum transactionStatusEnum = notifyTransactionStatus.get(transactionId);
            if (transactionStatusEnum != null) {
                transactionFacts.setTransactionStatus(transactionStatusEnum);
                this.proxyMap.get(transactionFacts.getTransactionType()).callback(transactionFacts);
                return;
            }

            List<TransactionFacts> list = transactionHolder.putIfAbsent(transactionId, Lists.newArrayList(transactionFacts));
            if (list != null) {
                list.add(transactionFacts);
            } else {
                String path = configLoad.get(ConfigConstants.TRANSACTION_POINT_ROOT_PATH, TransactionConstants.DEFAULT_FILE_PATH);
                //创建事务文件
                FileUtil.touch(path + transactionId + TransactionConstants.FILE_SUFFIX_WAIT);
            }
            //字段填充
            transactionFacts.setApplicationId(configLoad.getId());
            transactionFacts.setId(IdUtil.getSnowflake(1, 1).nextId());

            long traceId = IdUtil.getSnowflake(1, 1).nextId();

            CountDownLatch countDownLatch = new CountDownLatch(1);
            waitAckMap.put(traceId, countDownLatch);

            TransactionRegister transactionRegister = new TransactionRegister();
            transactionRegister.setTraceId(traceId);
            transactionRegister.setId(transactionId);
            transactionRegister.setTransactionFacts(transactionFacts);
            transmitSupport.send(transactionRegister);

            if (!this.await(transactionId, traceId, countDownLatch)) {
                this.rollback(transactionId);
            }
        } catch (ConnectionException connectionException) {
            this.rollback(transactionId);
        } catch (Exception e) {
            log.error("事务上传失败", e);
        } finally {
            lock.unlock();
        }
    }

    public void rollback(String transactionId) {
        log.info("本地回滚:{}", transactionId);

        String path = configLoad.get(ConfigConstants.TRANSACTION_POINT_ROOT_PATH, TransactionConstants.DEFAULT_FILE_PATH);

        if (FileUtil.exist(path + transactionId + TransactionConstants.FILE_SUFFIX_DONE)) {
            log.error("transactionId:{}已处理", transactionId);
            return;
        }

        FileUtil.del(path + transactionId + TransactionConstants.FILE_SUFFIX_WAIT);
        FileUtil.del(path + transactionId + TransactionConstants.FILE_SUFFIX_LOCK);
        FileUtil.del(path + transactionId + TransactionConstants.FILE_SUFFIX_STATE);

        List<TransactionFacts> list = transactionHolder.remove(transactionId);
        if (CollUtil.isEmpty(list)) {
            log.error("当前事务回调上下文为空,{}", transactionId);
            return;
        }

        for (TransactionFacts facts : list) {
            facts.setTransactionStatus(TransactionStatusEnum.FAIL);
            this.callback(facts);
        }

        FileUtil.touch(path + transactionId + TransactionConstants.FILE_SUFFIX_DONE);
    }

    public void timeout(String transactionId) {
        Lock lock = stripes.get(transactionId);
        lock.lock();
        try {
            String path = configLoad.get(ConfigConstants.TRANSACTION_POINT_ROOT_PATH, TransactionConstants.DEFAULT_FILE_PATH);

            if (FileUtil.exist(path + transactionId + TransactionConstants.FILE_SUFFIX_DONE)) {
                return;
            }

            log.info("事务超时处理:{}", transactionId);

            FileUtil.del(path + transactionId + TransactionConstants.FILE_SUFFIX_WAIT);
            FileUtil.del(path + transactionId + TransactionConstants.FILE_SUFFIX_LOCK);
            FileUtil.del(path + transactionId + TransactionConstants.FILE_SUFFIX_STATE);

            List<TransactionFacts> list = transactionHolder.remove(transactionId);
            if (CollUtil.isEmpty(list)) {
                log.error("当前事务回调上下文为空,{}", transactionId);
                return;
            }

            for (TransactionFacts facts : list) {
                facts.setTransactionStatus(TransactionStatusEnum.FAIL);
                this.callback(facts);
            }

            FileUtil.touch(path + transactionId + TransactionConstants.FILE_SUFFIX_DONE);
        } finally {
            lock.unlock();
        }
    }

    public void callback(TransactionFacts facts) {
        this.proxyMap.get(facts.getTransactionType()).callback(facts);
    }

    public void submitNotify(String transactionId, TransactionStatusEnum transactionStatus, List<TransactionFacts> transactionFacts) {
        if (!executorService.isShutdown()) {
            executorService.submit(() -> {
                Lock lock = stripes.get(transactionId);
                lock.lock();

                notifyTransactionStatus.put(transactionId, transactionStatus);

                String path = configLoad.get(ConfigConstants.TRANSACTION_POINT_ROOT_PATH, TransactionConstants.DEFAULT_FILE_PATH);
                try {
                    if (FileUtil.exist(path + transactionId + TransactionConstants.FILE_SUFFIX_DONE)) {
                        log.error("transactionId:{}已处理", transactionId);
                        return;
                    }

                    long cursor = 0;
                    //之前有过回调
                    if (!FileUtil.exist(path + transactionId + TransactionConstants.FILE_SUFFIX_WAIT)) {
                        if (!FileUtil.exist(path + transactionId + TransactionConstants.FILE_SUFFIX_LOCK)) {
                            log.error("transactionId:{}已处理", transactionId);

                            FileUtil.touch(path + transactionId + TransactionConstants.FILE_SUFFIX_DONE);
                            return;
                        }
                        //更新游标
                        String s = FileUtil.readUtf8String(path + transactionId + TransactionConstants.FILE_SUFFIX_LOCK);
                        if (StrUtil.isNotEmpty(s)) {
                            cursor = Convert.toLong(s, 0L);
                        }
                    }

                    if (!process.add(transactionId)) {
                        log.error("transactionId:{}正在处理中", transactionId);
                        return;
                    }

                    try {
                        //之前没有回调过
                        if (FileUtil.exist(path + transactionId + TransactionConstants.FILE_SUFFIX_WAIT)) {
                            FileUtil.rename(FileUtil.file(path + transactionId + TransactionConstants.FILE_SUFFIX_WAIT), transactionId + TransactionConstants.FILE_SUFFIX_LOCK, true);
                        }

                        List<TransactionFacts> list = transactionHolder.remove(transactionId);
                        if (CollUtil.isEmpty(list)) {
                            log.error("当前事务回调上下文为空,{}", transactionId);

                            if (CollUtil.isEmpty(transactionFacts)) {
                                log.error("当前事务回调为空，退出回调,{}", transactionId);
                                return;
                            }

                            list = transactionFacts;
                        }

                        for (TransactionFacts facts : list) {
                            Long id = facts.getId();
                            if (cursor >= id) {
                                log.info("当前事务游标已处理,游标:{},事务id:{}", cursor, transactionId);
                                continue;
                            }

                            facts.setTransactionStatus(transactionStatus);
                            this.proxyMap.get(facts.getTransactionType()).callback(facts);

                            FileUtil.writeUtf8String(facts.getId().toString(), path + transactionId + TransactionConstants.FILE_SUFFIX_LOCK);
                        }
                        //创建.done文件
                        FileUtil.touch(path + transactionId + TransactionConstants.FILE_SUFFIX_DONE);
                    } catch (Exception e) {
                        log.error("回调失败,transactionId:{}", transactionId, e);
                    } finally {
                        FileUtil.del(path + transactionId + TransactionConstants.FILE_SUFFIX_LOCK);
                        process.remove(transactionId);
                        //事务处理完成确认
                        if (CollUtil.isNotEmpty(transactionFacts)) {
                            TransactionSubmitAck transactionSubmitAck = new TransactionSubmitAck();
                            transactionSubmitAck.setTransactionId(transactionId);
                            transactionSubmitAck.setFactIds(transactionFacts.stream().map(TransactionFacts::getId).collect(Collectors.toList()));
                            transmitSupport.send(transactionSubmitAck);
                        }
                    }
                } finally {
                    FileUtil.del(path + transactionId + TransactionConstants.FILE_SUFFIX_STATE);
                    notifyTransactionStatus.remove(transactionId);

                    lock.unlock();
                }
            });
        }
    }

    public void close() {
        this.executorService.shutdown();
    }

    /**
     * 事务提交重试(netty启动时候调用)
     */
    public void transactionSubmitRetry() {
        String path = configLoad.get(ConfigConstants.TRANSACTION_POINT_ROOT_PATH, TransactionConstants.DEFAULT_FILE_PATH);

        List<File> files = FileUtil.loopFiles(path);
        if (CollUtil.isNotEmpty(files)) {
            for (File file : files) {
                String name = file.getName();

                if (StrUtil.endWith(name, TransactionConstants.FILE_SUFFIX_STATE)) {
                    String transactionId = StrUtil.subBefore(name, ".", true);

                    log.info("开启事务提交重试,事务id:{}", transactionId);

                    TransactionStatusEnum transactionStatusEnum = TransactionStatusEnum.queryByState(FileUtil.readUtf8String(file));
                    if (transactionStatusEnum != null) {
                        this.submit(transactionId, transactionStatusEnum);
                    }
                }
            }
        }
    }

    /**
     * 事务补偿扫描(程序启动时调用)
     */
    public void transactionCompensation() {
        long currentTimeMillis = System.currentTimeMillis();
        String path = configLoad.get(ConfigConstants.TRANSACTION_POINT_ROOT_PATH, TransactionConstants.DEFAULT_FILE_PATH);

        if (!FileUtil.exist(path)) {
            FileUtil.mkdir(path);
            return;
        }

        List<File> files = FileUtil.loopFiles(path);
        if (CollUtil.isNotEmpty(files)) {
            for (File file : files) {
                long l = file.lastModified();
                String name = file.getName();

                if (StrUtil.endWith(name, TransactionConstants.FILE_SUFFIX_WAIT) || StrUtil.endWith(name, TransactionConstants.FILE_SUFFIX_LOCK)) {
                    if (l <= currentTimeMillis) {
                        String transactionId = StrUtil.subBefore(name, ".", true);

                        log.info("开启事务补偿,事务id:{}", transactionId);

                        TransactionCompensation transactionCompensation = new TransactionCompensation();
                        transactionCompensation.setTransactionId(transactionId);
                        transactionCompensation.setApplicationId(configLoad.getId());
                        transmitSupport.send(transactionCompensation);
                    }
                }
            }
        }
    }

    /**
     * 阻塞等待
     *
     * @param id
     */
    private boolean await(String transactionId, Long id, CountDownLatch countDownLatch) {
        try {
            countDownLatch.await(120, TimeUnit.SECONDS);
            //等待超时
            if (countDownLatch.getCount() != 0L) {
                log.error("等待服务端响应超时,transactionId:{}", transactionId);
                return false;
            }

            AckTypeEnum ackTypeEnum = confirmAckMap.remove(id);
            if (ackTypeEnum == null) {
                log.error("服务端响应为空,transactionId:{}", transactionId);
                return false;
            }

            if (ackTypeEnum.equals(AckTypeEnum.ERROR)) {
                log.error("服务端处理失败,transactionId:{}", transactionId);
                return false;
            }

            return true;
        } catch (InterruptedException interruptedException) {
            return false;
        } finally {
            confirmAckMap.remove(id);
        }
    }
}
