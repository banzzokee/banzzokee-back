package homes.banzzokee.infra.firebase.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;
import homes.banzzokee.infra.firebase.FcmService;
import homes.banzzokee.infra.firebase.dto.TopicMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FcmServiceImpl implements FcmService {

  @Override
  public TopicManagementResponse subscribeToTopic(List<String> tokens, String topic)
      throws FirebaseMessagingException {
    return FirebaseMessaging.getInstance().subscribeToTopic(tokens, topic);
  }

  @Override
  public TopicManagementResponse unsubscribeFromTopic(List<String> tokens, String topic)
      throws FirebaseMessagingException {
    return FirebaseMessaging.getInstance().unsubscribeFromTopic(tokens, topic);
  }

  @Override
  public String sendTopicMessage(TopicMessage message)
      throws FirebaseMessagingException {
    return FirebaseMessaging.getInstance().send(Message.builder()
        .setNotification(Notification.builder()
            .setTitle(message.getTitle())
            .setBody(message.getBody())
            .setImage(message.getImage())
            .build())
        .setTopic(message.getTopic())
        .putAllData(message.getData())
        .build());
  }
}
