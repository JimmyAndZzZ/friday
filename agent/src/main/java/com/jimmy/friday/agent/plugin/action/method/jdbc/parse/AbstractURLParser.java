package com.jimmy.friday.agent.plugin.action.method.jdbc.parse;

import com.jimmy.friday.agent.base.ConnectionURLParser;
import com.jimmy.friday.agent.other.jdbc.URLLocation;

public abstract class AbstractURLParser implements ConnectionURLParser {

    protected String url;

    public AbstractURLParser(String url) {
        this.url = url;
    }

    protected abstract URLLocation fetchDatabaseHostsIndexRange();

    protected abstract URLLocation fetchDatabaseNameIndexRange();

    protected String fetchDatabaseHostsFromURL() {
        URLLocation hostsLocation = fetchDatabaseHostsIndexRange();
        return url.substring(hostsLocation.startIndex(), hostsLocation.endIndex());
    }

    protected String fetchDatabaseNameFromURL() {
        URLLocation hostsLocation = fetchDatabaseNameIndexRange();
        return url.substring(hostsLocation.startIndex(), hostsLocation.endIndex());
    }

    protected String fetchDatabaseNameFromURL(int[] indexRange) {
        return url.substring(indexRange[0], indexRange[1]);
    }

}
