package homes.banzzokee.consumer.fcm;

import com.google.firebase.messaging.BatchResponse;
import homes.banzzokee.consumer.error.exception.ChatMessageNotFoundException;
import homes.banzzokee.domain.chat.dao.ChatMessageRepository;
import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.notification.dao.FcmTokenRepository;
import homes.banzzokee.domain.notification.dao.NotificationRepository;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.domain.notification.entity.Notification;
import homes.banzzokee.domain.notification.entity.NotificationReceiver;
import homes.banzzokee.event.ChatMessageSendEvent;
import homes.banzzokee.event.dto.ChatMessagePayload;
import homes.banzzokee.infra.firebase.FcmService;
import homes.banzzokee.infra.firebase.dto.MultiMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
  public void handleEvent(ChatMessageSendEvent event) {
    ChatMessage chatMessage = findChatMessageOrThrow(event.getPayload().getMessageId());
    Notification notification = createNotification(event.getPayload(), chatMessage);
    notificationRepository.save(notification);

    try {
      MultiMessage message = createMultiMessage(notification, chatMessage);
      BatchResponse batchResponse = fcmService.sendMultiMessage(message);
      log.info("send message success, successCount={}, failureCount={}",
          batchResponse.getSuccessCount(),
          batchResponse.getFailureCount());
    } catch (Exception e) {
      log.error("send message failed", e);
    }
  }

  private Notification createNotification(ChatMessagePayload payload,
      ChatMessage chatMessage) {
    return Notification.of(
        "topic.chatroom." + payload.getRoomId(),
        payload.getSender() + "님이 메시지를 보냈습니다",
        payload.getMessage(),
        null,
        List.of(chatMessage.getReceiver()));
  }

  private ChatMessage findChatMessageOrThrow(Long chatMessageId) {
    return chatMessageRepository.findById(chatMessageId)
        .orElseThrow(() -> new ChatMessageNotFoundException(chatMessageId));
  }

  private MultiMessage createMultiMessage(Notification notification,
      ChatMessage chatMessage) {
    if (notification.getReceivers().isEmpty()) {
      log.trace("receivers is empty");
      return null;
    }

    Map<String, String> data = new HashMap<>();
    data.put("notificationId", notification.getId().toString());
    data.put("chatRoomId", chatMessage.getRoom().getId().toString());
    data.put("chatMessageId", chatMessage.getId().toString());

    NotificationReceiver receiver = notification.getReceivers().get(0);
    List<String> tokens = fcmTokenRepository.findAllByUserId(receiver.getUser().getId())
        .stream()
        .map(FcmToken::getToken)
        .toList();

    return MultiMessage.of(tokens,
        notification.getTitle(),
        notification.getBody(),
        notification.getImage(),
        data);
  }
}
