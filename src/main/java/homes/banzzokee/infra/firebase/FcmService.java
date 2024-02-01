package homes.banzzokee.infra.firebase;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import java.util.List;

public interface FcmService {

  TopicManagementResponse subscribeToTopic(List<String> registrationTokens,
      String topic) throws FirebaseMessagingException;
}
