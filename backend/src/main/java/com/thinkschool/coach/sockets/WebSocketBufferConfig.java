package com.thinkschool.coach.sockets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
public class WebSocketBufferConfig {

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {

        ServletServerContainerFactoryBean container =
                new ServletServerContainerFactoryBean();

        container.setMaxTextMessageBufferSize(10 * 1024 * 1024);   // 10MB
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024); // 10MB

        return container;
    }
}