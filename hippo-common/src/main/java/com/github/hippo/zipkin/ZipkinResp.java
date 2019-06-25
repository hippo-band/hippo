package com.github.hippo.zipkin;

import java.io.Serializable;

public class ZipkinResp implements Serializable {
    private static final long serialVersionUID = 4173049565572011911L;
    private String parentSpanId;

    private String parentTraceId;

    private Object span;

    private Object tracing;

    public Object getSpan() {
        return span;
    }

    public void setSpan(Object span) {
        this.span = span;
    }

    public Object getTracing() {
        return tracing;
    }

    public void setTracing(Object tracing) {
        this.tracing = tracing;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getParentTraceId() {
        return parentTraceId;
    }

    public void setParentTraceId(String parentTraceId) {
        this.parentTraceId = parentTraceId;
    }
}
