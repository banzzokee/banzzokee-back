package homes.banzzokee.global.config.stomp.handler;

import homes.banzzokee.global.config.stomp.principal.StompPrincipal;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * WebSocket HandshakeHandler
 * 메세지를 전송할때 handshake 진행
 */
@Slf4j
@Configuration
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

  // 메세지를 전송한 유저 name 반환
  @Override
  protected Principal determineUser(ServerHttpRequest request,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) {

    return new StompPrincipal(UUID.randomUUID().toString()); // random 유저 고유 id 생성

  }
}