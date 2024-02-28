package homes.banzzokee.global.config.rabbitmq;

import static homes.banzzokee.global.config.rabbitmq.Queue.DLQ_MANAGE_FCM_TOKEN;
import static homes.banzzokee.global.config.rabbitmq.Queue.DLQ_MANAGE_FCM_TOPIC;
import static homes.banzzokee.global.config.rabbitmq.Queue.DLQ_NOTIFY_FCM_ADOPTION;
import static homes.banzzokee.global.config.rabbitmq.Queue.DLQ_NOTIFY_FCM_REVIEW;
import static homes.banzzokee.global.config.rabbitmq.Queue.DLQ_SYNC_ELASTICSEARCH_ADOPTION;
import static homes.banzzokee.global.config.rabbitmq.Queue.DLQ_SYNC_ELASTICSEARCH_REVIEW;
import static homes.banzzokee.global.config.rabbitmq.Queue.MANAGE_FCM_TOKEN;
import static homes.banzzokee.global.config.rabbitmq.Queue.MANAGE_FCM_TOPIC;
import static homes.banzzokee.global.config.rabbitmq.Queue.NOTIFY_FCM_ADOPTION;
import static homes.banzzokee.global.config.rabbitmq.Queue.NOTIFY_FCM_CHAT;
import static homes.banzzokee.global.config.rabbitmq.Queue.NOTIFY_FCM_REVIEW;
import static homes.banzzokee.global.config.rabbitmq.Queue.SYNC_ELASTICSEARCH_ADOPTION;
import static homes.banzzokee.global.config.rabbitmq.Queue.SYNC_ELASTICSEARCH_REVIEW;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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
  private static final String DLQ_EXCHANGE_NAME = "dlq.topic";

  private Queue queueWithDlx(homes.banzzokee.global.config.rabbitmq.Queue queue) {
    return QueueBuilder.durable(queue.getName())
        .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE_NAME)
        .build();
  }

  private Queue queue(homes.banzzokee.global.config.rabbitmq.Queue queue) {
    return new Queue(queue.getName(), true);
  }

  @Bean
  TopicExchange exchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  @Bean
  TopicExchange dlqExchange() {
    return new TopicExchange(DLQ_EXCHANGE_NAME);
  }

  @Bean
  Queue syncEsAdoptionQueue() {
    return queueWithDlx(SYNC_ELASTICSEARCH_ADOPTION);
  }

  @Bean
  Queue syncEsReviewQueue() {
    return queueWithDlx(SYNC_ELASTICSEARCH_REVIEW);
  }

  @Bean
  Queue notifyFcmAdoption() {
    return queueWithDlx(NOTIFY_FCM_ADOPTION);
  }

  @Bean
  Queue notifyFcmReview() {
    return queueWithDlx(NOTIFY_FCM_REVIEW);
  }

  @Bean
  Queue notifyFcmChat() {
    return queue(NOTIFY_FCM_CHAT);
  }

  @Bean
  Queue manageFcmToken() {
    return queueWithDlx(MANAGE_FCM_TOKEN);
  }

  @Bean
  Queue manageFcmTopic() {
    return queueWithDlx(MANAGE_FCM_TOPIC);
  }


  @Bean
  Queue dlqSyncEsAdoptionQueue() {
    return queue(DLQ_SYNC_ELASTICSEARCH_ADOPTION);
  }

  @Bean
  Queue dlqSyncEsReviewQueue() {
    return queue(DLQ_SYNC_ELASTICSEARCH_REVIEW);
  }

  @Bean
  Queue dlqNotifyFcmAdoption() {
    return queue(DLQ_NOTIFY_FCM_ADOPTION);
  }

  @Bean
  Queue dlqNotifyFcmReview() {
    return queue(DLQ_NOTIFY_FCM_REVIEW);
  }

  @Bean
  Queue dlqManageFcmToken() {
    return queue(DLQ_MANAGE_FCM_TOKEN);
  }

  @Bean
  Queue dlqManageFcmTopic() {
    return queue(DLQ_MANAGE_FCM_TOPIC);
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
  Binding notifyFcmAdoptionStatusBinding() {
    return BindingBuilder
        .bind(notifyFcmAdoption())
        .to(exchange())
        .with("adoption.status.changed");
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
  Binding manageFcmTokenBinding() {
    return BindingBuilder
        .bind(manageFcmToken())
        .to(exchange())
        .with("token.registered");
  }

  @Bean
  Binding manageFcmTopicBinding() {
    return BindingBuilder
        .bind(manageFcmTopic())
        .to(exchange())
        .with("topic.#");
  }

  @Bean
  Binding dlqSyncEsAdoptionBinding() {
    return BindingBuilder
        .bind(dlqSyncEsAdoptionQueue())
        .to(dlqExchange())
        .with("adoption.#");
  }

  @Bean
  Binding dlqSyncEsReviewBinding() {
    return BindingBuilder
        .bind(dlqSyncEsReviewQueue())
        .to(dlqExchange())
        .with("review.#");
  }

  @Bean
  Binding dlqNotifyFcmAdoptionBinding() {
    return BindingBuilder
        .bind(dlqNotifyFcmAdoption())
        .to(dlqExchange())
        .with("adoption.created");
  }

  @Bean
  Binding dlqNotifyFcmAdoptionStatusBinding() {
    return BindingBuilder
        .bind(dlqNotifyFcmAdoption())
        .to(dlqExchange())
        .with("adoption.status.changed");
  }

  @Bean
  Binding dlqNotifyFcmReviewBinding() {
    return BindingBuilder
        .bind(dlqNotifyFcmReview())
        .to(dlqExchange())
        .with("review.created");
  }

  @Bean
  Binding dlqManageFcmTokenBinding() {
    return BindingBuilder
        .bind(dlqManageFcmToken())
        .to(dlqExchange())
        .with("token.registered");
  }

  @Bean
  Binding dlqManageFcmTopicBinding() {
    return BindingBuilder
        .bind(dlqManageFcmTopic())
        .to(dlqExchange())
        .with("topic.#");
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
