package com.jimmy.friday.boot.result;

import com.jimmy.friday.boot.core.agent.RunLine;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class TraceResult implements Serializable {

    private String ts;

    private Long traceId;

    private String threadName;

    private List<RunLine> runLines = new ArrayList<>();
}
