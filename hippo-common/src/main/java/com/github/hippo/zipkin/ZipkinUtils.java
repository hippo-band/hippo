package com.github.hippo.zipkin;

/**
 * zipkin 通用工具类
 */
public class ZipkinUtils {
    public static ZipkinResp zipkinRecordStart(ZipkinData zipkinData, ZipkinRecordService zipkinRecordService) {
        if (zipkinRecordService == null) {
            return null;
        }
        return zipkinRecordService.start(zipkinData);
    }

    public static void zipkinRecordFinish(ZipkinRecordService zipkinRecordService) {
        if (zipkinRecordService == null) {
            return;
        }
        zipkinRecordService.finish();
    }

    public static void zipkinRecordError(ZipkinRecordService zipkinRecordService, Throwable throwable) {
        if (zipkinRecordService == null) {
            return;
        }
        zipkinRecordService.error(throwable);
    }
}
