package homes.banzzokee.global.config.stomp;

import homes.banzzokee.global.config.stomp.handler.CustomHandshakeHandler;
import homes.banzzokee.global.config.stomp.handler.StompErrorHandler;
import homes.banzzokee.global.config.stomp.handler.StompPreHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
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

  private final StompPreHandler stompPreHandler;
  private final StompErrorHandler stompErrorHandler;
  private final CustomHandshakeHandler customHandshakeHandler;

  @Value("${spring.rabbitmq.host}")
  private String externalBrokerHost;

  @Value("${spring.rabbitmq.username}")
  private String externalBrokerLogin;

  @Value("${spring.rabbitmq.password}")
  private String externalBrokerPassword;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-stomp")
        // ws://localhost:8080/ws-stomp 로 연결
        .setAllowedOriginPatterns("*")
        .setHandshakeHandler(customHandshakeHandler) // Websocket hand-shake
        .withSockJS(); // SockJs 사용 가능 설정
    // http://localhost:8080/ws-stomp 로 연결 가능
    registry.setErrorHandler(stompErrorHandler);
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 구독 요청 /queue 는 1:1, /topic 은 1:N
    registry.enableStompBrokerRelay("/queue", "/topic")
        .setRelayHost(externalBrokerHost)
        .setClientLogin(externalBrokerLogin)
        .setClientPasscode(externalBrokerPassword);

    // client -> server 로 메세지 전송 요청할때 쓰일 접두사
    // MessageController 에서 쓰임 확인
    registry.setApplicationDestinationPrefixes("/api");
  }


  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // STOMP 연결 시도 시 호출되는 메서드
    registration.interceptors(stompPreHandler);
    // 연결을 시도하면 StompPreHandler 실행
  }

}
