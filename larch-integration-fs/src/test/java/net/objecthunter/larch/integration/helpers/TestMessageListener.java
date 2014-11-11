/*
 * Copyright 2014 FIZ Karlsruhe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.objecthunter.larch.integration.helpers;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMessageListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(TestMessageListener.class);

    private ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<>();

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            log.warn("Null message received");
            return;
        }
        messages.add(message);
        log.info("Received message");
    }

    public boolean isMessageReceived() {
        return !messages.isEmpty();
    }

    public Message getMessage() {
        return messages.poll();
    }
}
