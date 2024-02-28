package homes.banzzokee.consumer.fcm;

import com.google.firebase.messaging.BatchResponse;
import homes.banzzokee.consumer.error.exception.ChatMessageNotFoundException;
import homes.banzzokee.domain.chat.dao.ChatMessageRepository;
import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.notification.dao.FcmTokenRepository;
import homes.banzzokee.domain.notification.dao.NotificationRepository;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.domain.notification.entity.Notification;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.ChatMessageSendEvent;
import homes.banzzokee.infra.firebase.FcmService;
import homes.banzzokee.infra.firebase.dto.MultiMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageNotificationConsumer {

  private final NotificationRepository notificationRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final FcmTokenRepository fcmTokenRepository;
  private final FcmService fcmService;

  @Transactional
  @RabbitListener(queues = "queue.notify.fcm.chat", errorHandler = "customErrorHandler")
  public void handleEvent(@Payload ChatMessageSendEvent event,
      @Header(required = false, name = "x-death") Map<String, Object> xDeath,
      Message mqMessage) {
    ChatMessage chatMessage = findChatMessageOrThrow(event.getPayload().getMessageId());
    MultiMessage message = createMultiMessage(chatMessage);

    try {
      assert message != null;
      if (message.getTokens().isEmpty()) {
        log.info("message receiver's token is empty. this message will not send to fcm.");
        return;
      }
      BatchResponse batchResponse = fcmService.sendMultiMessage(message);
      log.info("send message success, successCount={}, failureCount={}",
          batchResponse.getSuccessCount(),
          batchResponse.getFailureCount());
    } catch (Exception e) {
      log.error("send message failed", e);
    }

    notificationRepository.save(createNotification(message, chatMessage.getReceiver()));
  }

  private Notification createNotification(MultiMessage message, User receiver) {
    return Notification.of(
        message.toJson(),
        List.of(receiver));
  }

  private ChatMessage findChatMessageOrThrow(Long chatMessageId) {
    return chatMessageRepository.findById(chatMessageId)
        .orElseThrow(() -> new ChatMessageNotFoundException(chatMessageId));
  }

  private MultiMessage createMultiMessage(ChatMessage chatMessage) {
    if (chatMessage.getReceiver() == null) {
      log.trace("receivers is empty");
      return null;
    }

    Map<String, String> data = new HashMap<>();
    data.put("chatRoomId", chatMessage.getRoom().getId().toString());
    data.put("chatMessageId", chatMessage.getId().toString());
    data.put("adoptionId", chatMessage.getRoom().getAdoption().getId().toString());

    List<String> tokens = fcmTokenRepository.findAllByUserId(
            chatMessage.getReceiver().getId())
        .stream()
        .map(FcmToken::getToken)
        .toList();

    return MultiMessage.of(tokens,
        chatMessage.getUser().getNickname() + "님이 메시지를 보냈습니다",
        chatMessage.getMessage(),
        chatMessage.getUser().getProfileImageUrl(),
        data);
  }
}
