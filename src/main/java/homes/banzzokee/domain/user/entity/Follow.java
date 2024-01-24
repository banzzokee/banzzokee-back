package homes.banzzokee.domain.user.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import homes.banzzokee.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팔로우
 */
@Entity
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

  /**
   * 팔로우 아이디
   */
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  /**
   * 팔로우 하는 사람
   */
  @ManyToOne(fetch = LAZY)
  private final User follower;

  /**
   * 팔로우 받는 사람
   */
  @ManyToOne(fetch = LAZY)
  private final User followee;

  @Builder
  public Follow(User follower, User followee) {
    this.follower = follower;
    this.followee = followee;
  }
}
