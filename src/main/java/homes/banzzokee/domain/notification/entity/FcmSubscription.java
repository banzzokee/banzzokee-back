package homes.banzzokee.domain.notification.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor(access = PROTECTED, force = true)
public class FcmSubscription extends BaseEntity {

  /**
   * FCM 구독 아이디
   */
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  /**
   * FCM 토큰
   */
  @ManyToOne
  private final FcmToken fcmToken;

  /**
   * 구독 토픽
   */
  private final String topic;

  /**
   * 구독 유효 여부(구독 성공, 실패)
   */
  private boolean isValid;
}
