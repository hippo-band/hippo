package com.github.hippo.zipkin;

import brave.Span;
import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.propagation.TraceContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import java.util.concurrent.TimeUnit;

/**
 * zipkin 实现类
 */
@Component
public class ZipkinRecordImpl implements ZipkinRecordService {


    private static final Logger LOGGER = LoggerFactory.getLogger(ZipkinRecordImpl.class);

    @Value("${hippo.zipkin.url:}")
    private String zipkinUrl;

    private static boolean isInitError;


    @Override
    public ZipkinResp start(ZipkinReq zipkinReq) {

        if (isInitError || zipkinReq == null || StringUtils.isBlank(zipkinUrl) || StringUtils.isBlank(zipkinReq.getServiceName())) {
            return null;
        }
        Tracing tracing = null;
        try {
            Span span;
            tracing = Tracing.newBuilder()
                    .localServiceName(zipkinReq.getServiceName())
                    .spanReporter(AsyncReporter.builder(URLConnectionSender.create(zipkinUrl))
                            .closeTimeout(100, TimeUnit.MILLISECONDS)
                            .build(SpanBytesEncoder.JSON_V2))
                    .propagationFactory(B3Propagation.FACTORY)
                    .currentTraceContext(ThreadLocalCurrentTraceContext.create())
                    .build();
            if (zipkinReq.getParentSpanId() != null && zipkinReq.getParentTraceId() != null) {
                span = tracing.tracer().newChild(TraceContext.newBuilder()
                        .parentId(zipkinReq.getParentSpanId())
                        .traceId(zipkinReq.getParentTraceId())
                        .spanId(zipkinReq.getParentSpanId()).build());
            } else {
                span = tracing.tracer().newTrace();
            }

            if (StringUtils.isNotBlank(zipkinReq.getAnnotate())) {
                span.annotate(zipkinReq.getAnnotate());
            }
            if (StringUtils.isNotBlank(zipkinReq.getMethodName())) {
                span.name(zipkinReq.getMethodName());
            }

            if (zipkinReq.getSpanKind() != null) {
                if (zipkinReq.getSpanKind() == SpanKind.CLIENT) {
                    span.kind(Span.Kind.CLIENT);
                } else {
                    span.kind(Span.Kind.SERVER);
                }
            }
            if (zipkinReq.getTags() != null && !zipkinReq.getTags().isEmpty()) {
                for (String key : zipkinReq.getTags().keySet()) {
                    span.tag(key, zipkinReq.getTags().get(key));
                }
            }
            span.start();
            ZipkinResp resp = new ZipkinResp();
            resp.setParentSpanId(Long.toHexString(span.context().spanId()));
            resp.setParentTraceId(Long.toHexString(span.context().traceId()));
            resp.setSpan(span);
            resp.setTracing(tracing);
            return resp;
        } catch (Exception e) {
            LOGGER.error("zipkin start error:" + zipkinUrl, e);
            close(tracing);
            isInitError = true;
        }
        return null;
    }

    private void close(Tracing tracing) {
        if (tracing != null) {
            tracing.close();
        }
    }

    @Override
    public void finish(ZipkinResp zipkinResp) {
        try {
            if (zipkinResp != null && zipkinResp.getSpan() != null) {
                ((Span) zipkinResp.getSpan()).finish();
            }
        } catch (Exception e) {
            LOGGER.error("zipkin finish error:" + zipkinUrl, e);
        } finally {
            close((Tracing) zipkinResp.getTracing());
        }
    }

    @Override
    public void error(ZipkinResp zipkinResp, Throwable throwable) {
        try {
            if (zipkinResp != null && zipkinResp.getSpan() != null) {
                ((Span) zipkinResp.getSpan()).error(throwable);
            }
        } catch (Exception e) {
            LOGGER.error("zipkin error error:" + zipkinUrl, e);
            close((Tracing) zipkinResp.getTracing());
        }
    }


    @Override
    public void finish(ZipkinResp zipkinResp, Throwable throwable) {
        try {
            if (zipkinResp != null && zipkinResp.getSpan() != null) {
                ((Span) zipkinResp.getSpan()).error(throwable);
                ((Span) zipkinResp.getSpan()).finish();
            }
        } catch (Exception e) {
            LOGGER.error("zipkin finish[throwable] error:" + zipkinUrl, e);
        } finally {
            close((Tracing) zipkinResp.getTracing());
        }
    }
}
