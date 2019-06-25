package com.github.hippo.server;

import com.github.hippo.zipkin.ZipkinRecordService;

public class ZipkinCache {
    private ZipkinRecordService zipkinRecordService;

    public ZipkinRecordService getZipkinRecordService() {
        return zipkinRecordService;
    }

    public void setZipkinRecordService(ZipkinRecordService zipkinRecordService) {
        this.zipkinRecordService = zipkinRecordService;
    }
}
