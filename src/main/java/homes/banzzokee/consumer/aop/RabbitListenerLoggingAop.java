package homes.banzzokee.consumer.aop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class RabbitListenerLoggingAop {

  @Before("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
  public void logRabbitListener(JoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();

    Optional<Message> message = Arrays.stream(args)
        .filter(arg -> arg instanceof Message)
        .findFirst().map(arg -> (Message) arg);

    Optional<? extends HashMap<?, ?>> death = Arrays.stream(args)
        .filter(arg -> arg instanceof HashMap<?, ?>).findFirst()
        .map(arg -> (HashMap<?, ?>) arg);

    if (message.isEmpty()) {
      log.info("message argument is null");
      return;
    }

    String queueName = message.get().getMessageProperties().getConsumerQueue();
    String routingKey = message.get().getMessageProperties().getReceivedRoutingKey();
    String payload = new String(message.get().getBody());

    if (death.isPresent()) {
      log.info("message dead lettered, retry={}, routingKey={}, queue={}, payload={}",
          death.get().get("count"), routingKey, queueName, payload);
    } else {
      log.info("message received, routingKey={}, queue={}, payload={}", routingKey,
          queueName, payload);
    }
  }
}
