package com.jimmy.friday.agent.other.jdbc;


import com.google.common.base.Strings;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionCache {

    private static final ConcurrentHashMap<String, ConnectionInfo> CONNECTIONS_MAP = new ConcurrentHashMap<>();

    private static final String CONNECTION_SPLIT_STR = ",";

    public static ConnectionInfo get(String host, String port, String databaseName) {
        final String hostPortPair = String.format("%s:%s/%s", host, port, databaseName);
        return CONNECTIONS_MAP.get(hostPortPair);
    }

    public static ConnectionInfo get(String hostPortPair, String databaseName) {
        return CONNECTIONS_MAP.get(hostPortPair + "/" + databaseName);
    }

    public static void save(ConnectionInfo connectionInfo) {
        for (String conn : connectionInfo.getDatabasePeer().split(CONNECTION_SPLIT_STR)) {
            if (!Strings.isNullOrEmpty(conn)) {
                CONNECTIONS_MAP.putIfAbsent(conn + "/" + connectionInfo.getDatabaseName(), connectionInfo);
            }
        }
    }
}
