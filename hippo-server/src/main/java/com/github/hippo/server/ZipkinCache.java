package com.github.hippo.server;

import com.github.hippo.zipkin.ZipkinRecordService;

public class ZipkinCache {
    private String serviceName;
    private ZipkinRecordService zipkinRecordService;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ZipkinRecordService getZipkinRecordService() {
        return zipkinRecordService;
    }

    public void setZipkinRecordService(ZipkinRecordService zipkinRecordService) {
        this.zipkinRecordService = zipkinRecordService;
    }
}
