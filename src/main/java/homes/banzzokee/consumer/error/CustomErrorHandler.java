package homes.banzzokee.consumer.error;

import homes.banzzokee.consumer.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

@Slf4j
public class CustomErrorHandler implements RabbitListenerErrorHandler {

  @Override
  public Object handleError(Message message,
      org.springframework.messaging.Message<?> message1,
      ListenerExecutionFailedException e) {
    String queueName = message.getMessageProperties().getConsumerQueue();
    String routingKey = message.getMessageProperties().getReceivedRoutingKey();
    String payload = new String(message.getBody());

    if (!(e.getCause() instanceof BusinessException be)) {
      log.error("error occurred. routingKey={}, queue={}, payload={}",
          routingKey, queueName, payload, e);

      throw new AmqpRejectAndDontRequeueException(
          "AmqpRejectAndDontRequeueException occurred", e);
    } else {
      log.error("error occurred. routingKey={}, queue={}, payload={}",
          routingKey, queueName, payload, be);
    }

    return null;
  }
}
