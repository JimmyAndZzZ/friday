package com.jimmy.friday.agent.other.jdbc;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class SqlSession {

    private static final Map<String, String> SESSION = Maps.newConcurrentMap();

    public static void put(String id, String sql, String param, Date date) {
        if (Strings.isNullOrEmpty(sql)) {
            return;
        }

        if (date == null) {
            date = new Date();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("time:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)).append(" ");
        sb.append(sql);

        if (!Strings.isNullOrEmpty(param)) {
            sb.append(" param:").append(param);
        }

        SESSION.put(id, sb.toString());
    }

    public static void remove(String id) {
        SESSION.remove(id);
    }

    public static Collection<String> list() {
        return SESSION.values();
    }
}
