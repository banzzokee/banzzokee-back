package homes.banzzokee.infra.rabbitmq.event.handler;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import homes.banzzokee.infra.rabbitmq.service.RabbitMqMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class MessageHandler {

  private final RabbitMqMessageProducer rabbitMqMessageProducer;

  @Async
  @TransactionalEventListener(classes = BaseMessage.class, phase = AFTER_COMMIT)
  public void handle(BaseMessage<?> message) {
    rabbitMqMessageProducer.publish(message);
  }
}
