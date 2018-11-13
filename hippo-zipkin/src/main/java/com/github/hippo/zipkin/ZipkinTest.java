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
        new ZipkinTest().asd();
        Tracing tracing = Tracing.newBuilder()
                .localServiceName("test1")
                .spanReporter(reporter)
                .propagationFactory(B3Propagation.FACTORY)
                .currentTraceContext(ThreadLocalCurrentTraceContext.create())
                .build();
        Tracer tracer = tracing.tracer();
        Span span = tracer.newTrace().annotate("111").start();
        // System.out.println(  span.context().traceId());
        //System.out.println(  span.context().spanId());
        //tracer.nextSpan().start();
        span.tag("tttt1", "1111");
        span.tag("tttt2", "1111");
        span.tag("tttt3", "1111");
//        Span span = tracer.newChild(TraceContext.newBuilder().parentId(-3644591365253961277L).traceId(-3644591365253961277L).spanId(8055838165201322580L).build()).name("eeeee").start();
//         System.out.println(  span.context().traceId());
//        System.out.println(  span.context().spanId());
        //System.out.println(span.annotate("").kind(Span.Kind.CLIENT));
        //Span action_1 = tracer.newChild(span.context()).name("action-1").start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            span.finish();
            //    action_1.finish();
        }
        Thread.sleep(100000000L);
    }

    private  void asd() {
        System.out.println(this.getClass().getCanonicalName() + "."
                + Thread.currentThread().getStackTrace()[1].getMethodName());
    }
}