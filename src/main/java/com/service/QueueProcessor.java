package com.service;

import com.intuit.ipp.data.Entity;
import com.intuit.ipp.data.EventNotification;
import com.intuit.ipp.data.WebhooksEvent;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.WebhooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class QueueProcessor implements Runnable {

    @Autowired
    private QueueService queueService;

    private Map<String, Calculative> serviceDictionary;

    @Autowired
    public QueueProcessor(Calculative... services) {
        serviceDictionary = new HashMap<>();
        for (Calculative service : services) {
            serviceDictionary.put(service.getEntityName(), service);
        }
    }

    private void update(Entity entity) throws FMSException {
        if (serviceDictionary.containsKey(entity.getName()))
            serviceDictionary.get(entity.getName()).process(entity);
    }

    @Override
    public void run() {
        while (!queueService.getQueue().isEmpty()) {
            String payload = queueService.getQueue().poll();
            WebhooksService service = new WebhooksService();
            WebhooksEvent event = service.getWebhooksEvent(payload);
            EventNotification eventNotification = event.getEventNotifications().get(0);
            for (Entity entity : eventNotification.getDataChangeEvent().getEntities()) {
                try {
                    update(entity);
                } catch (FMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
