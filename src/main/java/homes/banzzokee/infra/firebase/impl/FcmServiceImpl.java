package homes.banzzokee.infra.firebase.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import homes.banzzokee.infra.firebase.FcmService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FcmServiceImpl implements FcmService {

  public TopicManagementResponse subscribeToTopic(List<String> tokens, String topic)
      throws FirebaseMessagingException {
    return FirebaseMessaging.getInstance().subscribeToTopic(tokens, topic);
  }

  public TopicManagementResponse unsubscribeFromTopic(List<String> tokens, String topic)
      throws FirebaseMessagingException {
    return FirebaseMessaging.getInstance().unsubscribeFromTopic(tokens, topic);
  }
}
