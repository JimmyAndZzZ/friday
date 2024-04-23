package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayServiceMethodDao;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceMethod;
import com.jimmy.friday.center.service.GatewayServiceMethodService;
import com.jimmy.friday.center.service.GatewayServiceService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * (GatewayServiceMethod)表服务实现类
 *
 * @author makejava
 * @since 2023-12-08 14:17:23
 */
@Service("gatewayServiceMethodService")
public class GatewayServiceMethodServiceImpl extends ServiceImpl<GatewayServiceMethodDao, GatewayServiceMethod> implements GatewayServiceMethodService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private GatewayServiceService gatewayServiceService;

    @Override
    public Map<Long, Long> getMethodIdMapperServiceId() {
        QueryWrapper<GatewayServiceMethod> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "service_id");
        List<GatewayServiceMethod> list = this.list(queryWrapper);

        return list.stream().collect(Collectors.toMap(GatewayServiceMethod::getId, GatewayServiceMethod::getServiceId));
    }

    @Override
    public Integer getTodayMethodInvokeCount(Long methodId) {
        String attachment = attachmentCache.attachment(RedisConstants.GATEWAY_METHOD_TODAY_INVOKE_COUNT + methodId);
        return Convert.toInt(StrUtil.emptyToDefault(attachment, "0"), 0);
    }

    @Override
    public Long getLastInvokeTimestamp(Long methodId) {
        String attachment = attachmentCache.attachment(RedisConstants.GATEWAY_METHOD_LAST_INVOKE_TIME + methodId);
        return Convert.toLong(StrUtil.emptyToDefault(attachment, "0"), 0L);
    }

    @Override
    public Integer getHistoryMethodInvokeCount(Long methodId) {
        String attachment = attachmentCache.attachment(RedisConstants.GATEWAY_METHOD_HISTORY_INVOKE_COUNT + methodId);
        return Convert.toInt(StrUtil.emptyToDefault(attachment, "0"), 0);
    }

    @Override
    public GatewayServiceMethod getById(Serializable id) {
        return attachmentCache.attachment(RedisConstants.SERVICE_METHOD_CACHE, id.toString(), GatewayServiceMethod.class, () -> super.getById(id));
    }

    @Override
    public GatewayServiceMethod queryById(Serializable id) {
        return super.getById(id);
    }

    @Override
    public GatewayServiceMethod query(com.jimmy.friday.boot.core.gateway.Service service, Method method) {
        GatewayService gatewayService = gatewayServiceService.getGatewayService(service);

        QueryWrapper<GatewayServiceMethod> wrapper = new QueryWrapper<>();
        wrapper.eq("service_id", gatewayService.getId());
        wrapper.eq("method_id", method.getMethodId());
        return this.getOne(wrapper);
    }

    @Override
    public List<GatewayServiceMethod> queryByServiceId(Long serviceId) {
        QueryWrapper<GatewayServiceMethod> wrapper = new QueryWrapper<>();
        wrapper.eq("service_id", serviceId);
        return this.list(wrapper);
    }

    @Override
    public void removeByServiceId(Long serviceId) {
        this.remove(this.queryByServiceId(serviceId));
    }

    @Override
    public void remove(List<GatewayServiceMethod> gatewayServiceMethods) {
        if (CollUtil.isNotEmpty(gatewayServiceMethods)) {
            this.removeByIds(gatewayServiceMethods.stream().map(GatewayServiceMethod::getId).collect(Collectors.toList()));

            for (GatewayServiceMethod gatewayServiceMethod : gatewayServiceMethods) {
                attachmentCache.remove(RedisConstants.SERVICE_METHOD_CACHE, gatewayServiceMethod.getId().toString());
            }
        }
    }

    @Override
    public GatewayServiceMethod queryByMethod(Method method, Long serviceId) {
        if (method == null) {
            return null;
        }

        String methodId = method.getMethodId();
        return StrUtil.isNotEmpty(methodId) ? this.queryByMethodId(methodId, serviceId) : this.queryByMethodCode(SecureUtil.md5(method.key()), serviceId);
    }

    @Override
    public GatewayServiceMethod queryByMethod(String methodCode, String methodId, Long serviceId) {
        return StrUtil.isNotEmpty(methodId) ? this.queryByMethodId(methodId, serviceId) : this.queryByMethodCode(methodCode, serviceId);
    }

    @Override
    public GatewayServiceMethod queryByMethodId(String methodId, Long serviceId) {
        String key = StrUtil.builder().append(serviceId).append(":").append(methodId).toString();

        Object id = attachmentCache.attachment(RedisConstants.SERVICE_METHOD_ID_MAPPER, key);
        if (id == null) {
            QueryWrapper<GatewayServiceMethod> wrapper = new QueryWrapper<>();
            wrapper.eq("method_id", methodId);
            wrapper.eq("service_id", serviceId);
            GatewayServiceMethod one = this.getOne(wrapper);
            if (one == null) {
                return null;
            }

            attachmentCache.mapper(RedisConstants.SERVICE_METHOD_ID_MAPPER, key, one.getId());
            return one;
        }

        GatewayServiceMethod gatewayServiceMethod = this.getById(id.toString());
        if (gatewayServiceMethod == null) {
            attachmentCache.remove(RedisConstants.SERVICE_METHOD_ID_MAPPER, key);
        }

        return gatewayServiceMethod;
    }

    @Override
    public GatewayServiceMethod queryByMethodCode(String methodCode, Long serviceId) {
        String key = "MAPPER:METHOD:CODE:" + methodCode;

        Object id = attachmentCache.attachment(RedisConstants.SERVICE_METHOD_ID_MAPPER, key);
        if (id == null) {
            QueryWrapper<GatewayServiceMethod> wrapper = new QueryWrapper<>();
            wrapper.eq("method_code", key);
            wrapper.eq("service_id", serviceId);
            GatewayServiceMethod one = this.getOne(wrapper);
            if (one == null) {
                return null;
            }

            attachmentCache.mapper(RedisConstants.SERVICE_METHOD_ID_MAPPER, key, one.getId());
            return one;
        }

        GatewayServiceMethod gatewayServiceMethod = this.getById(id.toString());
        if (gatewayServiceMethod == null) {
            attachmentCache.remove(RedisConstants.SERVICE_METHOD_ID_MAPPER, key);
        }

        return gatewayServiceMethod;
    }

    @Override
    public boolean save(GatewayServiceMethod gatewayServiceMethod) {
        boolean save = super.save(gatewayServiceMethod);
        if (save) {
            attachmentCache.attach(RedisConstants.SERVICE_METHOD_CACHE, gatewayServiceMethod.getId().toString(), gatewayServiceMethod);
        }

        return save;
    }

    @Override
    public boolean updateById(GatewayServiceMethod gatewayServiceMethod) {
        boolean save = super.updateById(gatewayServiceMethod);
        if (save) {
            attachmentCache.attach(RedisConstants.SERVICE_METHOD_CACHE, gatewayServiceMethod.getId().toString(), gatewayServiceMethod);
        }

        return save;
    }

    @Override
    public boolean updateBatchById(Collection<GatewayServiceMethod> gatewayServiceMethods) {
        boolean save = super.updateBatchById(gatewayServiceMethods);
        if (save) {
            for (GatewayServiceMethod gatewayServiceMethod : gatewayServiceMethods) {
                attachmentCache.attach(RedisConstants.SERVICE_METHOD_CACHE, gatewayServiceMethod.getId().toString(), gatewayServiceMethod);
            }
        }

        return save;
    }

    @Override
    public boolean saveBatch(Collection<GatewayServiceMethod> gatewayServiceMethods) {
        boolean save = super.saveBatch(gatewayServiceMethods);
        if (save) {
            for (GatewayServiceMethod gatewayServiceMethod : gatewayServiceMethods) {
                attachmentCache.attach(RedisConstants.SERVICE_METHOD_CACHE, gatewayServiceMethod.getId().toString(), gatewayServiceMethod);
            }
        }

        return save;
    }
}

