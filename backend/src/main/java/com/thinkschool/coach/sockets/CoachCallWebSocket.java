package com.thinkschool.coach.sockets;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.thinkschool.coach.communication.CoachCallHandler;

@Configuration
@EnableWebSocket
public class CoachCallWebSocket implements WebSocketConfigurer {
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(new CoachCallHandler(), "/coach-call")
				.setAllowedOriginPatterns("*");
	}

}
