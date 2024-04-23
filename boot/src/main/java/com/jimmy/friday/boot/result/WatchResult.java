package com.jimmy.friday.boot.result;

import com.jimmy.friday.boot.core.agent.RunLine;
import lombok.Data;

import java.io.Serializable;

@Data
public class WatchResult implements Serializable {

    private Long traceId;

    private RunLine runLine;

    private String ts;
}
