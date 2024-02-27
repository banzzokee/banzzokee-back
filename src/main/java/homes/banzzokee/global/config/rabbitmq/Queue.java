package homes.banzzokee.global.config.rabbitmq;

import lombok.Getter;

@Getter
enum Queue {
  SYNC_ELASTICSEARCH_ADOPTION("queue.sync.elasticsearch.adoption"),
  SYNC_ELASTICSEARCH_REVIEW("queue.sync.elasticsearch.review"),
  NOTIFY_FCM_ADOPTION("queue.notify.fcm.adoption"),
  NOTIFY_FCM_REVIEW("queue.notify.fcm.review"),
  NOTIFY_FCM_CHAT("queue.notify.fcm.chat"),
  MANAGE_FCM_TOPIC("queue.manage.fcm.topic"),
  MANAGE_FCM_TOKEN("queue.manage.fcm.token"),
  DLQ_SYNC_ELASTICSEARCH_ADOPTION("dlq.sync.elasticsearch.adoption"),
  DLQ_SYNC_ELASTICSEARCH_REVIEW("dlq.sync.elasticsearch.review"),
  DLQ_NOTIFY_FCM_ADOPTION("dlq.notify.fcm.adoption"),
  DLQ_NOTIFY_FCM_REVIEW("dlq.notify.fcm.review"),
  DLQ_NOTIFY_FCM_CHAT("dlq.notify.fcm.chat"),
  DLQ_MANAGE_FCM_TOPIC("dlq.manage.fcm.topic"),
  DLQ_MANAGE_FCM_TOKEN("dlq.manage.fcm.token")
  ;

  private final String name;

  Queue(String name) {
    this.name = name;
  }
}
