package com.github.hippo.zipkin;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ZipkinTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        URLConnectionSender sender = URLConnectionSender.create("http://103.10.0.67:9411/api/v2/spans");
        AsyncReporter reporter = AsyncReporter.builder(sender)
                .closeTimeout(100, TimeUnit.MILLISECONDS)
                .build(SpanBytesEncoder.JSON_V2);
        Tracing tracing = Tracing.newBuilder()
                .localServiceName("test1")
                .spanReporter(reporter)
                .propagationFactory(B3Propagation.FACTORY)
                .currentTraceContext(ThreadLocalCurrentTraceContext.create())
                .build();
        Tracer tracer = tracing.tracer();
        tracer.startScopedSpan("t1");
        Span span = tracer.newTrace().annotate("111").start();
        span.kind(Span.Kind.CLIENT);
       // span1.finish();
        // System.out.println(  span.context().traceId());
        //System.out.println(  span.context().spanId());
        //tracer.nextSpan().start();

       // Span span2=tracer.newChild(TraceContext.newBuilder().parentId(span.context().spanId()).traceId(span.context().traceId()).spanId(span.context().spanId()).build()).name("eeeee").annotate("222").start();
        // Span span2 = tracer.newChild(TraceContext.newBuilder().parentId(span.context().spanId()).traceId(span.context().traceId()).spanId(span.context().spanId()).build()).name("eeeee").start();
       // span2.kind(Span.Kind.SERVER);
//         System.out.println(  span.context().traceId());
//        System.out.println(  span.context().spanId());
        //System.out.println(span.annotate("").kind(Span.Kind.CLIENT));
        //Span action_1 = tracer.newChild(span.context()).name("action-1").start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //span1.finish();
            span.finish();
            //    action_1.finish();
        }
        Thread.sleep(100000000L);
    }

}