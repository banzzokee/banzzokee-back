package homes.banzzokee.global.config.rabbitmq;

import static homes.banzzokee.global.config.rabbitmq.Queue.NOTIFY_FCM_ADOPTION;
import static homes.banzzokee.global.config.rabbitmq.Queue.NOTIFY_FCM_CHAT;
import static homes.banzzokee.global.config.rabbitmq.Queue.NOTIFY_FCM_REVIEW;
import static homes.banzzokee.global.config.rabbitmq.Queue.NOTIFY_FCM_TOKEN;
import static homes.banzzokee.global.config.rabbitmq.Queue.SYNC_ELASTICSEARCH_ADOPTION;
import static homes.banzzokee.global.config.rabbitmq.Queue.SYNC_ELASTICSEARCH_REVIEW;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

  private static final String EXCHANGE_NAME = "amq.topic";

  private Queue queue(homes.banzzokee.global.config.rabbitmq.Queue queue) {
    return new Queue(queue.getName(), true);
  }

  @Bean
  TopicExchange exchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  @Bean
  Queue syncEsAdoptionQueue() {
    return queue(SYNC_ELASTICSEARCH_ADOPTION);
  }

  @Bean
  Queue syncEsReviewQueue() {
    return queue(SYNC_ELASTICSEARCH_REVIEW);
  }

  @Bean
  Queue notifyFcmAdoption() {
    return queue(NOTIFY_FCM_ADOPTION);
  }

  @Bean
  Queue notifyFcmReview() {
    return queue(NOTIFY_FCM_REVIEW);
  }

  @Bean
  Queue notifyFcmChat() {
    return queue(NOTIFY_FCM_CHAT);
  }

  @Bean
  Queue notifyFcmToken() {
    return queue(NOTIFY_FCM_TOKEN);
  }

  @Bean
  Binding syncEsAdoptionBinding() {
    return BindingBuilder
        .bind(syncEsAdoptionQueue())
        .to(exchange())
        .with("adoption.#");
  }

  @Bean
  Binding syncEsReviewBinding() {
    return BindingBuilder
        .bind(syncEsReviewQueue())
        .to(exchange())
        .with("review.#");
  }

  @Bean
  Binding notifyFcmAdoptionBinding() {
    return BindingBuilder
        .bind(notifyFcmAdoption())
        .to(exchange())
        .with("adoption.created");
  }

  @Bean
  Binding notifyFcmReviewBinding() {
    return BindingBuilder
        .bind(notifyFcmReview())
        .to(exchange())
        .with("review.created");
  }

  @Bean
  Binding notifyFcmChatBinding() {
    return BindingBuilder
        .bind(notifyFcmChat())
        .to(exchange())
        .with("message.send");
  }

  @Bean
  Binding notifyFcmTokenBinding() {
    return BindingBuilder
        .bind(notifyFcmToken())
        .to(exchange())
        .with("token.registered");
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    // TODO 재전송 정책
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setExchange(EXCHANGE_NAME);
    rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
    return rabbitTemplate;
  }

  @Bean
  public MessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
