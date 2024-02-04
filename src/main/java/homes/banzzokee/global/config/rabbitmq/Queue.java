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
  ;

  private final String name;

  Queue(String name) {
    this.name = name;
  }
}
