package com.jimmy.friday.boot.core.agent;


import com.jimmy.friday.boot.other.ShortUUID;
import lombok.Data;

import java.io.Serializable;

@Data
public class Context implements Serializable {

    private String traceId;

    private String spanId;

    private String classPoint;

    private String methodPoint;

    private Boolean isNeedPushLog = false;

    public Context() {
        this.traceId = ShortUUID.uuid();
        this.spanId = ShortUUID.uuid();
    }
}
