package com.jimmy.friday.agent.other.jdbc;

public class PreparedStatementParameterBuilder {
    private static int SQL_PARAMETERS_MAX_LENGTH = 512;

    private static final String EMPTY_LIST = "[]";

    private Object[] parameters;

    private Integer maxIndex;

    public PreparedStatementParameterBuilder setParameters(Object[] parameters) {
        this.parameters = parameters;
        return this;
    }

    public PreparedStatementParameterBuilder setMaxIndex(int maxIndex) {
        this.maxIndex = maxIndex;
        return this;
    }

    public String build() {
        if (parameters == null) {
            return EMPTY_LIST;
        }

        return getParameterString();
    }

    private String getParameterString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < getMaxIndex(); i++) {
            Object parameter = parameters[i];
            if (!first) {
                stringBuilder.append(",");
            }
            stringBuilder.append(parameter);
            first = false;

            if (SQL_PARAMETERS_MAX_LENGTH > 0 && (stringBuilder.length() + EMPTY_LIST.length()) > SQL_PARAMETERS_MAX_LENGTH) {
                return format(stringBuilder).substring(0, SQL_PARAMETERS_MAX_LENGTH) + "...";
            }
        }
        return format(stringBuilder);
    }

    private int getMaxIndex() {
        int maxIdx = maxIndex != null ? maxIndex : parameters.length;
        return Math.min(maxIdx, parameters.length);
    }

    private String format(StringBuilder stringBuilder) {
        return String.format("[%s]", stringBuilder.toString());
    }

}
