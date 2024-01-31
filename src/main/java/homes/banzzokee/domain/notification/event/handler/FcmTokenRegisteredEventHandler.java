package homes.banzzokee.domain.notification.event.handler;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import homes.banzzokee.domain.notification.event.FcmTokenRegisteredEvent;
import homes.banzzokee.infra.rabbitmq.service.RabbitMqMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class FcmTokenRegisteredEventHandler {

  private final RabbitMqMessageProducer rabbitMqMessageProducer;

  @Async
  @TransactionalEventListener(classes = FcmTokenRegisteredEvent.class, phase = AFTER_COMMIT)
  public void handle(FcmTokenRegisteredEvent event) {
    rabbitMqMessageProducer.publish("token.registered", event);
  }
}
