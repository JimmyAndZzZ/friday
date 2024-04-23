package com.jimmy.friday.agent.other.jdbc;

import lombok.Data;

@Data
public class ConnectionInfo {

    private String dbType;

    private String databaseName;

    private String databasePeer;

    public ConnectionInfo(String dbType, String host, int port, String databaseName) {
        this.dbType = dbType;
        this.databasePeer = host + ":" + port;
        this.databaseName = databaseName;
    }

    public ConnectionInfo(String dbType, String hosts, String databaseName) {
        this.dbType = dbType;
        this.databasePeer = hosts;
        this.databaseName = databaseName;
    }
}
