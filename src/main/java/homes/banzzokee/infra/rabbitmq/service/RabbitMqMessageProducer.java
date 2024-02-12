package homes.banzzokee.infra.rabbitmq.service;

import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqMessageProducer {

  private final RabbitTemplate rabbitTemplate;

  public void publish(BaseMessage<?> event) {
    rabbitTemplate.convertAndSend(event.getRoutingKey(), event);
  }
}
