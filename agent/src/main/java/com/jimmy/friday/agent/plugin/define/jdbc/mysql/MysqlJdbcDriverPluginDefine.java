package com.jimmy.friday.agent.plugin.define.jdbc.mysql;

import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.plugin.define.JdbcDriverPluginDefine;

import static com.jimmy.friday.agent.match.MultiClassNameMatch.byMultiClassMatch;

public class MysqlJdbcDriverPluginDefine extends JdbcDriverPluginDefine {

    private static final String WITNESS_MYSQL_5X_CLASS = "com.mysql.jdbc.ConnectionImpl";

    @Override
    public ClassMatch enhanceClass() {
        return byMultiClassMatch("com.mysql.jdbc.Driver", "com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.NonRegisteringDriver");
    }

    @Override
    public String[] witnessClasses() {
        return new String[]{WITNESS_MYSQL_5X_CLASS};
    }

}
