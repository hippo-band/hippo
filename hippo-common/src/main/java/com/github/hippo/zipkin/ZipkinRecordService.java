package com.github.hippo.zipkin;

/**
 * 调用链接口(连Zipkin)
 */
public interface ZipkinRecordService {


    ZipkinResp start(ZipkinData zipkinData);

    void finish();

    void error(Throwable throwable);

    void finish(Throwable throwable);

}
