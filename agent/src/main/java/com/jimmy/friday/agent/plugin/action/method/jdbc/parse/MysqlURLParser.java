package com.jimmy.friday.agent.plugin.action.method.jdbc.parse;

import com.jimmy.friday.agent.other.jdbc.ConnectionInfo;
import com.jimmy.friday.agent.other.jdbc.URLLocation;

public class MysqlURLParser extends AbstractURLParser {

    private static final int DEFAULT_PORT = 3306;
    private String dbType = "Mysql";

    public MysqlURLParser(String url) {
        super(url);
    }

    public MysqlURLParser(String url, String dbType) {
        super(url);
        this.dbType = dbType;
    }

    @Override
    protected URLLocation fetchDatabaseHostsIndexRange() {
        int hostLabelStartIndex = url.indexOf("//");
        int hostLabelEndIndex = url.indexOf("/", hostLabelStartIndex + 2);
        int hostLabelEndIndexWithParameter = url.indexOf("?", hostLabelStartIndex + 2);
        if (hostLabelEndIndex == -1) {
            hostLabelEndIndex = hostLabelEndIndexWithParameter;
        }
        if (hostLabelEndIndexWithParameter < hostLabelEndIndex && hostLabelEndIndexWithParameter != -1) {
            hostLabelEndIndex = hostLabelEndIndexWithParameter;
        }
        if (hostLabelEndIndex == -1) {
            hostLabelEndIndex = url.length();
        }
        return new URLLocation(hostLabelStartIndex + 2, hostLabelEndIndex);
    }

    protected String fetchDatabaseNameFromURL(int startSize) {
        URLLocation hostsLocation = fetchDatabaseNameIndexRange(startSize);
        if (hostsLocation == null) {
            return "";
        }
        return url.substring(hostsLocation.startIndex(), hostsLocation.endIndex());
    }

    protected URLLocation fetchDatabaseNameIndexRange(int startSize) {
        int databaseStartTag = url.indexOf("/", startSize);
        int parameterStartTag = url.indexOf("?", startSize);
        if (parameterStartTag < databaseStartTag && parameterStartTag != -1) {
            return null;
        }
        if (databaseStartTag == -1) {
            return null;
        }
        int databaseEndTag = url.indexOf("?", databaseStartTag);
        if (databaseEndTag == -1) {
            databaseEndTag = url.length();
        }
        return new URLLocation(databaseStartTag + 1, databaseEndTag);
    }

    @Override
    protected URLLocation fetchDatabaseNameIndexRange() {
        int databaseStartTag = url.lastIndexOf("/");
        int databaseEndTag = url.indexOf("?", databaseStartTag);
        if (databaseEndTag == -1) {
            databaseEndTag = url.length();
        }
        return new URLLocation(databaseStartTag + 1, databaseEndTag);
    }

    @Override
    public ConnectionInfo parse() {
        URLLocation location = fetchDatabaseHostsIndexRange();
        String hosts = url.substring(location.startIndex(), location.endIndex());
        String[] hostSegment = hosts.split(",");
        if (hostSegment.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String host : hostSegment) {
                if (host.split(":").length == 1) {
                    sb.append(host).append(":").append(DEFAULT_PORT).append(",");
                } else {
                    sb.append(host).append(",");
                }
            }
            return new ConnectionInfo(dbType, sb.substring(0, sb.length() - 1), fetchDatabaseNameFromURL());
        } else {
            String[] hostAndPort = hostSegment[0].split(":");
            if (hostAndPort.length != 1) {
                return new ConnectionInfo(dbType, hostAndPort[0], Integer.valueOf(hostAndPort[1]), fetchDatabaseNameFromURL(location
                        .endIndex()));
            } else {
                return new ConnectionInfo(dbType, hostAndPort[0], DEFAULT_PORT, fetchDatabaseNameFromURL(location
                        .endIndex()));
            }
        }
    }

}
