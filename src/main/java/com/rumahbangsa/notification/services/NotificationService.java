package com.rumahbangsa.notification.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.smallrye.context.impl.wrappers.ContextualRunnable;
import io.smallrye.context.impl.wrappers.ContextualRunnableN;
import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class NotificationService {

    Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    ThreadContext threadContext;

    @Inject
    Mailer mailer;

    ObjectMapper om = new ObjectMapper();

    @Incoming("send-email-html")
    @Merge(Merge.Mode.MERGE)
    @SuppressWarnings("unchecked")
    public CompletableFuture<Void> incomingSendEmailHtml(Message<String> message){
        return managedExecutor.runAsync(threadContext.contextualRunnable(()->{
            try {
                Map<String, Object> payload = om.readValue(message.getPayload(), Map.class);

                String body = (String) payload.get("body");
                String subject = (String) payload.get("subject");
                String to = (String) payload.get("to");
                mailer.send(Mail.withHtml(to, subject, body));

                message.ack();
            } catch (Exception e){
                LOGGER.error(e.getMessage());
            }
        }));
    }
}
