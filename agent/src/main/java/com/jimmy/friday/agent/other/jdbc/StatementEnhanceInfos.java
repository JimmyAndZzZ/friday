package com.jimmy.friday.agent.other.jdbc;

import java.util.Arrays;

public class StatementEnhanceInfos {
    private ConnectionInfo connectionInfo;
    private String statementName;
    private String sql;
    private Object[] parameters;
    private int maxIndex = 0;

    public StatementEnhanceInfos(ConnectionInfo connectionInfo, String sql, String statementName) {
        this.connectionInfo = connectionInfo;
        this.sql = sql;
        this.statementName = statementName;
    }

    public StatementEnhanceInfos(ConnectionInfo connectionInfo, String statementName) {
        this.connectionInfo = connectionInfo;
        this.statementName = statementName;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public String getSql() {
        return sql;
    }

    public String getStatementName() {
        return statementName;
    }

    public void setParameter(int index, final Object parameter) {
        maxIndex = maxIndex > index ? maxIndex : index;
        index--; // start from 1
        if (parameters == null) {
            final int initialSize = Math.max(16, maxIndex);
            parameters = new Object[initialSize];
            Arrays.fill(parameters, null);
        }
        int length = parameters.length;
        if (index >= length) {
            int newSize = Math.max(index + 1, length * 2);
            Object[] newParameters = new Object[newSize];
            System.arraycopy(parameters, 0, newParameters, 0, length);
            Arrays.fill(newParameters, length, newSize, null);
            parameters = newParameters;
        }
        parameters[index] = parameter;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public int getMaxIndex() {
        return maxIndex;
    }
}
