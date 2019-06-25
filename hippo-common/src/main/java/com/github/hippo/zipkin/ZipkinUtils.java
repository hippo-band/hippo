package com.github.hippo.zipkin;

/**
 * zipkin 通用工具类
 */
public class ZipkinUtils {

    public static ZipkinResp zipkinRecordStart(ZipkinReq zipkinReq, ZipkinRecordService zipkinRecordService) {
        if (zipkinRecordService == null) {
            return null;
        }
        return zipkinRecordService.start(zipkinReq);
    }

    public static void zipkinRecordFinish(ZipkinResp zipkinResp, ZipkinRecordService zipkinRecordService) {
        if (zipkinRecordService == null) {
            return;
        }
        zipkinRecordService.finish(zipkinResp);
    }

    public static void zipkinRecordError(ZipkinResp zipkinResp, ZipkinRecordService zipkinRecordService, Throwable throwable) {
        if (zipkinRecordService == null) {
            return;
        }
        zipkinRecordService.error(zipkinResp,throwable);
    }

    public static void zipkinRecordFinish(ZipkinResp zipkinResp, ZipkinRecordService zipkinRecordService, Throwable throwable) {
        if (zipkinRecordService == null) {
            return;
        }
        zipkinRecordService.finish(zipkinResp,throwable);
    }
}
