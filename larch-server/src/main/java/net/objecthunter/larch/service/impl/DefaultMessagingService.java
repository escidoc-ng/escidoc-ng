/*
* Copyright 2014 Frank Asseg
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.objecthunter.larch.service.impl;

import net.objecthunter.larch.service.MessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

public class DefaultMessagingService implements MessagingService {
    private static final Logger log = LoggerFactory.getLogger(DefaultMessagingService.class);

    @Autowired
    private JmsTemplate template;

    @Autowired
    private Environment env;

    private boolean enabled;

    @PostConstruct
    public void init() {
        final String en = env.getProperty("larch.messaging.enabled");
        enabled = en != null && en.equalsIgnoreCase("true");
    }

    private void publish(final String text) {
        if (!enabled) {
            return;
        }
        this.template.send(new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(text);
            }
        });
    }

    @Override
    public void publishCreateEntity(String entityId) {
        this.publish("Created entity " + entityId);
    }

    @Override
    public void publishUpdateEntity(String entityId) {
        this.publish("Update entity " + entityId);
    }

    @Override
    public void publishDeleteEntity(String entityId) {
        this.publish("Deleted entity " + entityId);
    }

    @Override
    public void publishCreateBinary(String entityId, String binaryName) {
        this.publish("Created binary " + binaryName + " on entity " + entityId);
    }

    @Override
    public void publishUpdateBinary(String entityId, String binaryName) {
        this.publish("Updated binary " + binaryName + " on entity " + entityId);
    }

    @Override
    public void publishDeleteBinary(String entityId, String binaryName) {
        this.publish("Deleted binary " + binaryName + " on entity " + entityId);
    }

    @Override
    public void publishCreateMetadata(String entityId, String mdName) {
        this.publish("Created meta data " + mdName + " on entity " + entityId);
    }

    @Override
    public void publishUpdateMetadata(String entityId, String mdName) {
        this.publish("Updated meta data " + mdName + " on entity " + entityId);
    }

    @Override
    public void publishDeleteMetadata(String entityId, String mdName) {
        this.publish("Deleted meta data " + mdName + " on entity " + entityId);

    }

    @Override
    public void publishCreateBinaryMetadata(String entityId, String binaryName, String mdName) {
        this.publish("Created meta data " + mdName + " on binary " + binaryName + " of entity " + entityId);
    }

    @Override
    public void publishUpdateBinaryMetadata(String entityId, String binaryName, String mdName) {
        this.publish("Updated meta data " + mdName + " on binary " + binaryName + " of entity " + entityId);
    }

    @Override
    public void publishDeleteBinaryMetadata(String entityId, String binaryName, String mdName) {
        this.publish("Deleted meta data " + mdName + " on binary " + binaryName + " of entity " + entityId);
    }
}
