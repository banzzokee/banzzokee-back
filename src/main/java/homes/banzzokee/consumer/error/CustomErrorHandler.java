package homes.banzzokee.consumer.error;

import homes.banzzokee.consumer.error.exception.BusinessException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

public class CustomErrorHandler implements RabbitListenerErrorHandler {

  @Override
  public Object handleError(Message message,
      org.springframework.messaging.Message<?> message1,
      ListenerExecutionFailedException e) {
    if (!(e.getCause() instanceof BusinessException)) {
      throw new AmqpRejectAndDontRequeueException(
          "Error Handler converted exception to fatal", e);
    }
    return null;
  }
}
