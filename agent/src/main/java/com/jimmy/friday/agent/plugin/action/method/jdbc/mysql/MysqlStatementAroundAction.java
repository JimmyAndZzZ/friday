package com.jimmy.friday.agent.plugin.action.method.jdbc.mysql;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.other.jdbc.ConnectionInfo;
import com.jimmy.friday.agent.other.jdbc.SqlSession;
import com.jimmy.friday.agent.other.jdbc.StatementEnhanceInfos;
import com.jimmy.friday.agent.plugin.action.method.CmdMethodsAroundAction;
import com.jimmy.friday.agent.support.RunTopologySupport;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Date;

@Slf4j
public class MysqlStatementAroundAction extends CmdMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        if (param.length > 0) {
            String sql = (String) param[0];

            String sqlId = ContextHold.getSqlId();
            SqlSession.put(sqlId, sql, null, new Date());

            EnhancedField enhancedInstanceDynamicField = enhancedInstance.getDynamicField();
            if (enhancedInstanceDynamicField != null) {
                StatementEnhanceInfos cacheObject = (StatementEnhanceInfos) enhancedInstanceDynamicField.getDynamic();

                if (cacheObject != null) {
                    ConnectionInfo connectionInfo = cacheObject.getConnectionInfo();
                    if (connectionInfo != null) {
                        Context context = ContextHold.getContext();
                        if (context != null && context.getIsNeedPushLog()) {
                            String dbType = connectionInfo.getDbType();
                            String databaseName = connectionInfo.getDatabaseName();
                            String databasePeer = connectionInfo.getDatabasePeer();

                            Iterable<String> parts = Splitter.on(",").split(databasePeer);
                            for (String part : parts) {
                                String[] split = part.split(":");
                                if (split.length != 2) {
                                    continue;
                                }

                                Topology topology = new Topology();
                                topology.setMachine(split[0]);
                                topology.setApplication(dbType.toLowerCase());
                                topology.setType(dbType.toLowerCase());

                                String invokeRemark = dbType + "/" + part + "?" + databaseName;

                                RunTopologySupport.getInstance().send(ConfigLoad.getDefault().getTopology(), topology, invokeRemark, dbType.toLowerCase(), context.getTraceId());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[]
            param, Object result, Long cost) {
        if (param.length > 0) {
            super.afterMethod(enhancedInstance, method, parameterTypes, param, result, cost);
        }
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[]
            parameterTypes, Object[] param, Throwable throwable, Long cost) {
        if (param.length > 0) {
            super.handleMethodException(enhancedInstance, method, parameterTypes, param, throwable, cost);
        }
    }

    @Override
    public void ultimate(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[]
            param) {
        String sqlId = ContextHold.getAndRemoveSqlId();
        if (!Strings.isNullOrEmpty(sqlId)) {
            SqlSession.remove(sqlId);
        }
    }
}
