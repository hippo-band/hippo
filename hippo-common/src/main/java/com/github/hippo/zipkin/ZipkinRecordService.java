package com.github.hippo.zipkin;

/**
 * 调用链接口(连Zipkin)
 */
public interface ZipkinRecordService {


    ZipkinResp start(ZipkinReq zipkinReq);

    void finish(ZipkinResp zipkinResp);

    void error(ZipkinResp zipkinResp, Throwable throwable);

    void finish(ZipkinResp zipkinResp, Throwable throwable);

}
