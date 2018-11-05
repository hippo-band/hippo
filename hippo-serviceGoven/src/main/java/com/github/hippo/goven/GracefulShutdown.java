package com.github.hippo.goven;

import com.github.hippo.govern.ServiceGovern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class GracefulShutdown implements ApplicationListener<ContextClosedEvent> {
    @Autowired
    private ServiceGovern serviceGovern;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        serviceGovern.shutdown();
    }
}
