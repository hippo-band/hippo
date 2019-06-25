package com.github.hippo.zipkin;


import java.util.Map;

/**
 * zipkin data
 */
public class ZipkinReq {
    private SpanKind spanKind;
    private String annotate;
    private String methodName;

    private String serviceName;
    /**
     * null为root节点
     */
    private Long parentSpanId;

    private Long parentTraceId;

    private Map<String, String> tags;

    public SpanKind getSpanKind() {
        return spanKind;
    }

    public void setSpanKind(SpanKind spanKind) {
        this.spanKind = spanKind;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Long getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(Long parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public Long getParentTraceId() {
        return parentTraceId;
    }

    public void setParentTraceId(Long parentTraceId) {
        this.parentTraceId = parentTraceId;
    }

    public String getAnnotate() {
        return annotate;
    }

    public void setAnnotate(String annotate) {
        this.annotate = annotate;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
