package homes.banzzokee.global.config.rabbitmq;

import lombok.Getter;

@Getter
enum Queue {
  SYNC_ELASTICSEARCH_ADOPTION("queue.sync.elasticsearch.adoption"),
  SYNC_ELASTICSEARCH_REVIEW("queue.sync.elasticsearch.review"),
  NOTIFY_FCM_ADOPTION("queue.notify.fcm.adoption"),
  NOTIFY_FCM_REVIEW("queue.notify.fcm.review"),
  NOTIFY_FCM_CHAT("queue.notify.fcm.chat"),
  NOTIFY_FCM_TOKEN("queue.notify.fcm.token"),
  MANAGE_FCM_SUBSCRIPTION("queue.manage.fcm.subscription"),
  MANAGE_FCM_TOKEN("queue.manage.fcm.token"),
  ;

  private final String name;

  Queue(String name) {
    this.name = name;
  }
}
