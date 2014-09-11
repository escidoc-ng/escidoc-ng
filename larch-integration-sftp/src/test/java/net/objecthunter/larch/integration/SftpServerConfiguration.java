package net.objecthunter.larch.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SftpServerConfiguration {

    @Bean
    public SftpServer sftpServer() {
        return new SftpServer();
    }
}
