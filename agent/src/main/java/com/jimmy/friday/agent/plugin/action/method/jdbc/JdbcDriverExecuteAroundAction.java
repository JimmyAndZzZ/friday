package com.jimmy.friday.agent.plugin.action.method.jdbc;

import com.google.common.base.Splitter;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.other.jdbc.ConnectionCache;
import com.jimmy.friday.agent.other.jdbc.ConnectionInfo;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;
import com.jimmy.friday.agent.plugin.action.method.jdbc.parse.URLParser;
import com.jimmy.friday.agent.support.TopologySupport;

import java.lang.reflect.Method;

public class JdbcDriverExecuteAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        ConnectionInfo parser = URLParser.parser((String) param[0]);
        if (parser != null) {
            ConnectionCache.save(parser);

            String dbType = parser.getDbType();
            String databasePeer = parser.getDatabasePeer();

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

                String invokeRemark = dbType + "/" + part + "?" + parser.getDatabaseName();

                TopologySupport.getInstance().push(ConfigLoad.getDefault().getTopology(), topology, invokeRemark, dbType.toLowerCase());
            }
        }
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {

    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }
}
