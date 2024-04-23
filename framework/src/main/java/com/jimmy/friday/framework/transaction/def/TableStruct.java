package com.jimmy.friday.framework.transaction.def;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TableStruct implements Serializable {

    private String tableName;

    private List<String> primaryKeys = Lists.newArrayList();
}
