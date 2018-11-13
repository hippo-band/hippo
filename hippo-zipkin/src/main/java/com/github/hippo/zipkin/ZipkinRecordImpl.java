package com.github.hippo.zipkin;

import brave.Span;
import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.propagation.TraceContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import java.util.concurrent.TimeUnit;

@Component
public class ZipkinRecordImpl implements ZipkinRecordService {

    @Value("${hippo.zipkin.url}")
    private String zipkinUrl;

    private ThreadLocal<Span> spanThreadLocal = new ThreadLocal<>();

    private ThreadLocal<Tracing> tracingThreadLocal = new ThreadLocal<>();

    private void init(String serviceName) {
        if (StringUtils.isBlank(zipkinUrl)) {
            return;
        }
        if (tracingThreadLocal.get() != null) {
            return;
        }
        synchronized (ZipkinRecordImpl.class) {
            if (tracingThreadLocal.get() != null) {
                return;
            }
            Tracing tracing = Tracing.newBuilder()
                    .localServiceName(serviceName)
                    .spanReporter(AsyncReporter.builder(URLConnectionSender.create(zipkinUrl))
                            .closeTimeout(100, TimeUnit.MILLISECONDS)
                            .build(SpanBytesEncoder.JSON_V2))
                    .propagationFactory(B3Propagation.FACTORY)
                    .currentTraceContext(ThreadLocalCurrentTraceContext.create())
                    .build();
            tracingThreadLocal.set(tracing);
        }
    }

    @Override
    public ZipkinResp start(ZipkinData zipkinData) {

        if (zipkinData == null || StringUtils.isBlank(zipkinUrl) || StringUtils.isBlank(zipkinData.getServiceName())) {
            return null;
        }
        ZipkinResp resp = new ZipkinResp();
        init(zipkinData.getServiceName());
        Span span;
        if (zipkinData.getParentSpanId() != null && zipkinData.getParentTraceId() != null) {
            span = tracingThreadLocal.get().tracer().newChild(TraceContext.newBuilder()
                    .parentId(zipkinData.getParentSpanId())
                    .traceId(zipkinData.getParentTraceId())
                    .spanId(zipkinData.getParentSpanId()).build());
        } else {
            span = tracingThreadLocal.get().tracer().newTrace();
        }

        if (zipkinData.getSpanKind() != null) {
            if (zipkinData.getSpanKind() == SpanKind.CLIENT) {
                span.kind(Span.Kind.CLIENT);
            } else {
                span.kind(Span.Kind.SERVER);
            }
        }
        if (StringUtils.isNotBlank(zipkinData.getAnnotate())) {
            span.annotate(zipkinData.getAnnotate());
        }
        if (StringUtils.isNotBlank(zipkinData.getMethodName())) {
            span.name(zipkinData.getMethodName());
        }

        if (zipkinData.getTags() != null && !zipkinData.getTags().isEmpty()) {
            for (String key : zipkinData.getTags().keySet()) {
                span.tag(key, zipkinData.getTags().get(key));
            }
        }

        try {
            span.start();
            resp.setParentSpanId(Long.toHexString(span.context().spanId()));
            resp.setParentTraceId(Long.toHexString(span.context().traceId()));
            spanThreadLocal.set(span);
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
        return resp;
    }

    private void close() {
        if (spanThreadLocal.get() != null) {
            spanThreadLocal.remove();
        }
        if (tracingThreadLocal.get() != null) {
            tracingThreadLocal.get().close();
            tracingThreadLocal.remove();
        }
    }

    @Override
    public void finish() {
        try {
            if (spanThreadLocal.get() != null) {
                spanThreadLocal.get().finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    @Override
    public void error(Throwable throwable) {
        try {
            if (spanThreadLocal.get() != null) {
                spanThreadLocal.get().error(throwable);
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }


    @Override
    public void finish(Throwable throwable) {
        try {
            if (spanThreadLocal.get() != null) {
                spanThreadLocal.get().error(throwable);
                spanThreadLocal.get().finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }
}
