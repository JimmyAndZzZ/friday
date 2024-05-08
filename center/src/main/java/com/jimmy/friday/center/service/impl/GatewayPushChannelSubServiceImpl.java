package com.jimmy.friday.center.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.center.base.Obtain;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.dao.GatewayPushChannelSubDao;
import com.jimmy.friday.center.entity.GatewayPushChannelSub;
import com.jimmy.friday.center.service.GatewayPushChannelSubService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * (GatewayPushChannelSub)表服务实现类
 *
 * @author makejava
 * @since 2024-02-19 11:37:51
 */
@Service("gatewayPushChannelSubService")
public class GatewayPushChannelSubServiceImpl extends ServiceImpl<GatewayPushChannelSubDao, GatewayPushChannelSub> implements GatewayPushChannelSubService {

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public Long getCurrentOffset(String channelName, String appId) {
        String attachment = attachmentCache.attachment(RedisConstants.Gateway.GATEWAY_CHANNEL_CURRENT_OFFSET + channelName + ":" + appId);
        return StrUtil.isEmpty(attachment) ? 0L : Convert.toLong(attachment, 0L);
    }

    @Override
    public void updateCurrentOffset(String channelName, String appId, Long currentOffset) {
        attachmentCache.attachString(RedisConstants.Gateway.GATEWAY_CHANNEL_CURRENT_OFFSET + channelName + ":" + appId, currentOffset.toString());
    }

    @Override
    public void deleteCurrentOffset(String channelName, String appId) {
        attachmentCache.remove(RedisConstants.Gateway.GATEWAY_CHANNEL_CURRENT_OFFSET + channelName + ":" + appId);
    }

    @Override
    public List<GatewayPushChannelSub> queryByChannelName(String channelName) {
        return attachmentCache.attachmentList(RedisConstants.Gateway.GATEWAY_CHANNEL_SUB + channelName, GatewayPushChannelSub.class, new Obtain<List<GatewayPushChannelSub>>() {
            @Override
            public List<GatewayPushChannelSub> obtain() {
                QueryWrapper<GatewayPushChannelSub> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("channel_name", channelName);
                return list(queryWrapper);
            }
        });
    }

    @Override
    public GatewayPushChannelSub getById(Serializable id) {
        return attachmentCache.attachment(RedisConstants.Gateway.GATEWAY_CHANNEL_CACHE + id, GatewayPushChannelSub.class, () -> GatewayPushChannelSubServiceImpl.super.getById(id));
    }

    @Override
    public GatewayPushChannelSub queryByAccountIdAndChannelName(Long accountId, String channelName) {
        QueryWrapper<GatewayPushChannelSub> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("channel_name", channelName);
        queryWrapper.eq("account_id", accountId);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean removeById(Serializable id) {
        GatewayPushChannelSub byId = super.getById(id);
        if (byId == null) {
            return false;
        }

        attachmentCache.remove(RedisConstants.Gateway.GATEWAY_CHANNEL_CACHE + id);
        attachmentCache.remove(RedisConstants.Gateway.GATEWAY_CHANNEL_SUB + byId.getChannelName());

        return super.removeById(id);
    }

    @Override
    public boolean updateById(GatewayPushChannelSub gatewayPushChannelSub) {
        boolean b = super.updateById(gatewayPushChannelSub);
        if (b) {
            attachmentCache.remove(RedisConstants.Gateway.GATEWAY_CHANNEL_CACHE + gatewayPushChannelSub.getChannelName());
            attachmentCache.remove(RedisConstants.Gateway.GATEWAY_CHANNEL_SUB + gatewayPushChannelSub.getChannelName());
        }

        return b;
    }

    @Override
    public boolean save(GatewayPushChannelSub gatewayPushChannelSub) {
        Long accountId = gatewayPushChannelSub.getAccountId();
        String channelName = gatewayPushChannelSub.getChannelName();

        QueryWrapper<GatewayPushChannelSub> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("channel_name", channelName);
        queryWrapper.eq("account_id", accountId);
        if (this.count(queryWrapper) > 0) {
            return false;
        }

        return stripedLock.tryLock(RedisConstants.Gateway.GATEWAY_CHANNEL_SUB_SAVE_LOCK + accountId + ":" + channelName, 120L, TimeUnit.SECONDS, new Obtain<Boolean>() {

            @Override
            public Boolean obtain() {
                attachmentCache.remove(RedisConstants.Gateway.GATEWAY_CHANNEL_SUB + channelName);
                boolean save = GatewayPushChannelSubServiceImpl.super.save(gatewayPushChannelSub);
                attachmentCache.remove(RedisConstants.Gateway.GATEWAY_CHANNEL_SUB + channelName);
                return save;
            }
        }, false);
    }
}

