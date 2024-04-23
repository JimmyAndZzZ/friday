package com.jimmy.friday.framework.transaction.def;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Data
public class StatementInfo implements Serializable {

    private Statement statement;

    private Connection connection;

    private String statementQuery;

    private List<String> batch = Lists.newArrayList();
}
