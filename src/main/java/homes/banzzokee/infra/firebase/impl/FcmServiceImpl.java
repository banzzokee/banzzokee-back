package homes.banzzokee.infra.firebase.impl;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;
import homes.banzzokee.infra.firebase.FcmService;
import homes.banzzokee.infra.firebase.dto.MultiMessage;
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
    return FirebaseMessaging.getInstance().send(message.toFcmMessage());
  }

  @Override
  public BatchResponse sendMultiMessage(MultiMessage message)
      throws FirebaseMessagingException {


    return FirebaseMessaging.getInstance().sendEachForMulticast(message.toFcmMessage());
  }
}
