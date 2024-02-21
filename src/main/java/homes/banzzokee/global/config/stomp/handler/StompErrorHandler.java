package homes.banzzokee.global.config.stomp.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import homes.banzzokee.global.config.stomp.exception.SocketException;
import homes.banzzokee.global.error.ErrorResponse;
import homes.banzzokee.global.error.GlobalExceptionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

/**
 * websocket 관련 에러 반환
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StompErrorHandler extends StompSubProtocolErrorHandler {

  private final GlobalExceptionHandler globalExceptionHandler;
  private final ObjectMapper objectMapper;

  /**
   * 클라이언트 메세지 처리 중 에러 처리
   *
   * @param clientMessage 클라이언트 메세지
   * @param ex            발생한 예외
   * @return 에러 메세지를 포함한 Message 객체
   */
  @Override
  public Message<byte[]> handleClientMessageProcessingError(
      Message<byte[]> clientMessage, Throwable ex) {

    Throwable cause = ex.getCause();

    try {
      // 에러코드가 SocketException 일때
      if (cause instanceof SocketException se) {
        log.error("{} is occurred.", cause.getClass().getSimpleName(), se);
        return prepareErrorMessage(
            clientMessage,
            Objects.requireNonNull(
                // ExceptionHandler에 등록해둔 handleMessageException 형태로 반환
                globalExceptionHandler
                    .handleMessageException((SocketException) cause).getBody())
        );
      }
    } catch (Exception e) {
      return super.handleClientMessageProcessingError(clientMessage, e);
    }

    return super.handleClientMessageProcessingError(clientMessage, ex);
  }

  // 메세지 생성
  private Message<byte[]> prepareErrorMessage(Message<byte[]> clientMessage,
      ErrorResponse body)
      throws JsonProcessingException {

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
    // header message 로 에러 원인 반환
    accessor.setMessage(body.getMessage());
    accessor.setLeaveMutable(true);

    setReceiptIdForClient(clientMessage, accessor);

    // 직렬화 후 반환 -> console 에 띄움
    return MessageBuilder.createMessage(objectMapper.writeValueAsString(body).getBytes(
        StandardCharsets.UTF_8), accessor.getMessageHeaders());
  }

  private void setReceiptIdForClient(final Message<byte[]> clientMessage,
      final StompHeaderAccessor accessor) {
    if (Objects.isNull(clientMessage)) {
      return;
    }

    final StompHeaderAccessor clientHeaderAccessor = MessageHeaderAccessor.getAccessor(
        clientMessage, StompHeaderAccessor.class
    );

    final String receiptId =
        Objects.isNull(clientHeaderAccessor) ? null : clientHeaderAccessor.getReceiptId();

    if (receiptId != null) {
      accessor.setReceiptId(receiptId);
    }
  }
}