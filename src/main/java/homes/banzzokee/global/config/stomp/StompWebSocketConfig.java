package homes.banzzokee.global.config.stomp;

import homes.banzzokee.global.config.stomp.handler.CustomHandshakeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP configuration
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final CustomHandshakeHandler customHandshakeHandler;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-stomp")
        // ws://localhost:8080/ws-stomp 로 연결
        .setAllowedOriginPatterns("*")
        .setHandshakeHandler(customHandshakeHandler) // Websocket hand-shake
        .withSockJS(); // SockJs 사용 가능 설정
        // http://localhost:8080/ws-stomp 로 연결 가능
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/queue", "/topic");
    // 구독 요청 /queue 는 1:1, /topic 은 1:N

    registry.setApplicationDestinationPrefixes("/api");
    // client -> server 로 메세지 전송 요청할때 쓰일 접두사
    // MessageController 에서 쓰임 확인
  }

}
