
package com.jimmy.friday.agent.plugin.action.method.jdbc.parse;

import com.jimmy.friday.agent.base.ConnectionURLParser;
import com.jimmy.friday.agent.other.jdbc.ConnectionInfo;

public class URLParser {

    private static final String MYSQL_JDBC_URL_PREFIX = "jdbc:mysql";

    public static ConnectionInfo parser(String url) {
        ConnectionURLParser parser = null;
        String lowerCaseUrl = url.toLowerCase();
        if (lowerCaseUrl.startsWith(MYSQL_JDBC_URL_PREFIX)) {
            parser = new MysqlURLParser(url);
        }

        return parser.parse();
    }
}
