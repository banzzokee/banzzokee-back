package homes.banzzokee.consumer.elasticsearch;

import homes.banzzokee.consumer.error.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.event.EntityEvent;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncAdoptionToElasticSearchConsumer {

  private final AdoptionRepository adoptionRepository;
  private final AdoptionSearchRepository adoptionSearchRepository;

  @Transactional(readOnly = true)
  @RabbitListener(queues = {
      "queue.sync.elasticsearch.adoption",
      "dlq.sync.elasticsearch.adoption"
  }, errorHandler = "customErrorHandler")
  public void handleEvent(@Payload EntityEvent event,
      @Header(required = false, name = "x-death") Map<String, Object> xDeath,
      Message mqMessage) {

    Long adoptionId = event.getPayload().getId();

    Adoption adoption = adoptionRepository.findById(adoptionId)
        .orElseThrow(() -> new AdoptionNotFoundException(adoptionId));

    adoptionSearchRepository.save(AdoptionDocument.fromEntity(adoption));
  }
}
