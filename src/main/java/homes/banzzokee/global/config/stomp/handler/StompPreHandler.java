package homes.banzzokee.global.config.stomp.handler;

import homes.banzzokee.domain.chat.service.ChatMessageService;
import homes.banzzokee.global.config.stomp.exception.SocketAccessTokenExpiredException;
import homes.banzzokee.global.config.stomp.exception.SocketTokenInvalidException;
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
import org.springframework.messaging.simp.stomp.StompCommand;
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
  private static final int TOKEN_SPLIT_DEFAULT_VALUE = 7;

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {

    log.info("[preSend] STOMP 연결 확인");
    StompHeaderAccessor headerAccessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    // 웹소켓 연결시 호출
    if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
      log.info("[messagePreSend] connected");

      String token = resolveToken(headerAccessor.getFirstNativeHeader("Authorization"));

      try {
        if (token != null) {  // token 이 null 인지 만료되지는 않았는지 확인
          jwtTokenProvider.validateToken(token); // 유효성 체크
          Authentication authentication = jwtTokenProvider.getAuthentication(token); // 만료되지 않았다면 권한 목록 추출
          SecurityContextHolder.getContext().setAuthentication(authentication); // SpringSecurity 에 권한 목록 넘겨줌
          log.info("[doFilterInternal] token 값 유효성 체크 완료");

          // WS Header에 유저 저장
          headerAccessor.setUser(authentication);
        }
      } catch (ExpiredJwtException e) {
        // accessToken 이 만료되었을때
        log.error("[doFilterInternal] accessToken 만료");

        throw new SocketAccessTokenExpiredException();
      } catch (JwtException | IllegalArgumentException e) {
        // 토큰의 타입이 잘못된 타입이면,
        log.error("[doFilterInternal] 잘못된 타입의 토큰 에러");

        throw new SocketTokenInvalidException();
      }

      log.info("[preSendConnected] user : {}", Objects.requireNonNull(
          headerAccessor.getUser()).getName());
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
