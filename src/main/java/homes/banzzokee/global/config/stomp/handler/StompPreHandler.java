package homes.banzzokee.global.config.stomp.handler;

import static org.springframework.messaging.simp.stomp.StompCommand.CONNECT;
import static org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT;
import static org.springframework.messaging.simp.stomp.StompCommand.SUBSCRIBE;
import static org.springframework.messaging.simp.stomp.StompCommand.UNSUBSCRIBE;

import homes.banzzokee.global.config.stomp.exception.SocketAccessTokenExpiredException;
import homes.banzzokee.global.config.stomp.exception.SocketTokenInvalidException;
import homes.banzzokee.global.config.stomp.exception.SocketTokenRequiredException;
import homes.banzzokee.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

/**
 * Websocket 접근 interceptor
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // Security 보다 먼저 Interceptor 처리를 하기 위함
@Configuration
@RequiredArgsConstructor
public class StompPreHandler implements ChannelInterceptor {

  private static final String BEARER = "Bearer ";
  private static final int TOKEN_SPLIT_DEFAULT_VALUE = BEARER.length();

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor headerAccessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    // 웹소켓 연결시 호출
    assert headerAccessor != null;
    if (CONNECT.equals(headerAccessor.getCommand())) {
      log.info("[preSend] request for stomp connection. sessionId : {}",
          headerAccessor.getSessionId());

      String token = resolveToken(headerAccessor.getFirstNativeHeader("Authorization"));
      if (token == null) {
        throw new SocketTokenRequiredException();
      }

      try {
        if (token != null) {
          // 유효성 체크
          jwtTokenProvider.validateToken(token);

          // 만료되지 않았다면 권한 목록 추출
          Authentication authentication = jwtTokenProvider.getAuthentication(token);

          // SpringSecurity 에 권한 목록 넘겨줌
          SecurityContextHolder.getContext().setAuthentication(authentication);

          // WS Header에 유저 저장
          headerAccessor.setUser(authentication);
          log.info("[preSend] success stomp connection. user : {}, sessionId : {}",
              Objects.requireNonNull(headerAccessor.getUser()).getName(),
              headerAccessor.getSessionId());
        }

      } catch (ExpiredJwtException e) {
        throw new SocketAccessTokenExpiredException();
      } catch (JwtException | IllegalArgumentException e) {
        throw new SocketTokenInvalidException();
      }
    } else if (DISCONNECT.equals(headerAccessor.getCommand())) {
      // TODO: DISCONNECT 두번 처리 되는 증상이 확인 됨
      log.info("[preSend] stomp disconnect. user: {}, sessionId : {}",
          Objects.requireNonNull(headerAccessor.getUser()).getName(),
          headerAccessor.getSessionId());
    } else if (SUBSCRIBE.equals(headerAccessor.getCommand())) {
      log.info("[preSend] stomp subscribe. destination: {}, user: {}, sessionId : {}",
          headerAccessor.getDestination(),
          Objects.requireNonNull(headerAccessor.getUser()).getName(),
          headerAccessor.getSessionId());
    } else if (UNSUBSCRIBE.equals(headerAccessor.getCommand())) {
      log.info("[preSend] stomp unsubscribe. destination: {}, user: {}, sessionId : {}",
          headerAccessor.getDestination(),
          Objects.requireNonNull(headerAccessor.getUser()).getName(),
          headerAccessor.getSessionId());
    }

    return message;
  }

  private String resolveToken(String bearerToken) {
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
      return bearerToken.substring(TOKEN_SPLIT_DEFAULT_VALUE);
    }
    return null;
  }
}
