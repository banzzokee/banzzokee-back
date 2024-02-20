package homes.banzzokee.infra.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import homes.banzzokee.infra.firebase.dto.TopicMessage;
import java.util.List;

public interface FcmService {

  TopicManagementResponse subscribeToTopic(List<String> tokens, String topic)
      throws FirebaseMessagingException;

  TopicManagementResponse unsubscribeFromTopic(List<String> tokens, String topic)
      throws FirebaseMessagingException;

  String sendTopicMessage(TopicMessage message)
      throws FirebaseMessagingException, JsonProcessingException;
}
