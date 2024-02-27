package homes.banzzokee.consumer.elasticsearch;

import homes.banzzokee.consumer.error.exception.ReviewNotFoundException;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.review.dao.ReviewRepository;
import homes.banzzokee.domain.review.entity.Review;
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
public class SyncReviewToElasticSearchConsumer {

  private final AdoptionSearchRepository adoptionSearchRepository;
  private final ReviewRepository reviewRepository;

  @Transactional(readOnly = true)
  @RabbitListener(queues = {
      "queue.sync.elasticsearch.review",
      "dlq.sync.elasticsearch.review"
  }, errorHandler = "customErrorHandler")
  public void handleEvent(@Payload EntityEvent event,
      @Header(required = false, name = "x-death") Map<String, Object> xDeath,
      Message mqMessage) {

    Long reviewId = event.getPayload().getId();

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ReviewNotFoundException(reviewId));

    adoptionSearchRepository.save(AdoptionDocument.fromEntity(review.getAdoption()));
  }
}
