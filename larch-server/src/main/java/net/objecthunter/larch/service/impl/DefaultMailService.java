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

package net.objecthunter.larch.service.impl;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.service.MailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class DefaultMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(DefaultMailService.class);

    private InternetAddress mailFrom;

    private String smtpHost;

    private int smtpPort;

    private String mailUser;

    private String mailPass;

    private boolean enabled;

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        if (env.getProperty("larch.mail.enabled") == null ||
                env.getProperty("larch.mail.enabled").equalsIgnoreCase("false")) {
            enabled = false;
            return;
        }
        try {
            final String value = env.getProperty("larch.mail.from");
            if (value.isEmpty()) {
                throw new IllegalArgumentException("The property larch.mail.from cannot be blank");
            }
            mailFrom = new InternetAddress(value);
            smtpHost = env.getProperty("larch.mail.smtp.host", "localhost");
            smtpPort = Integer.parseInt(env.getProperty("larch.mail.smtp.port", "25"));
            enabled = Boolean.parseBoolean(env.getProperty("larch.mail.enabled", "false"));
            if (env.getProperty("larch.mail.smtp.user") != null) {
                mailUser = env.getProperty("larch.mail.smtp.user");
                mailPass = env.getProperty("larch.mail.smtp.pass");
            }
        } catch (AddressException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void sendUserRequest(UserRequest req) throws IOException {
        if (!enabled) {
            throw new IOException("Mail service is disabled. Unable to comply with request");
        }
        final Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        final Session sess;
        if (mailUser != null && !mailUser.isEmpty()) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", true);
            props.setProperty("mail.user", mailUser);
            props.setProperty("mail.password", mailPass);
            final Authenticator auth = new Authenticator() {

                final PasswordAuthentication pwAuth = new PasswordAuthentication(mailUser, mailPass);

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return pwAuth;
                }
            };
            sess = Session.getDefaultInstance(props, auth);
        } else {
            sess = Session.getDefaultInstance(props);
        }
        MimeMessage msg = new MimeMessage(sess);
        try {
            msg.setFrom(mailFrom);
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(req.getUser().getEmail()));
            msg.setSubject("User confirmation for larch repository");
            msg.setText("Please open the following link to confirm your account creation:\n" +
                    "http://localhost:8080/confirm?token=" + req.getToken());
            Transport.send(msg);
            log.info("Sent confirmation email to " + req.getUser().getEmail());
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
