package homes.banzzokee.domain.notification.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.common.entity.BaseEntity;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED, force = true)
public class FcmToken extends BaseEntity {

  /**
   * FCM 토큰 아이디
   */
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  /**
   * FCM 토큰
   */
  @Column(unique = true)
  private final String token;

  /**
   * FCM 토큰을 등록한 디바이스의 User-Agent
   */
  private final String userAgent;

  /**
   * FCM 토큰 소유자
   */
  @ManyToOne(fetch = LAZY)
  private final User user;

  /**
   * FCM 토큰의 마지막 사용 일시(사용 갱신)
   */
  private LocalDateTime lastUsedAt;

  @Builder
  protected FcmToken(String token, String userAgent, User user) {
    this.token = token;
    this.userAgent = userAgent;
    this.user = user;
    this.lastUsedAt = LocalDateTime.now();
  }

  /**
   * FCM 토큰의 사용 일시를 갱신한다.
   */
  public void refresh() {
    lastUsedAt = LocalDateTime.now();
  }
}
