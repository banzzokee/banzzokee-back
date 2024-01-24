package homes.banzzokee.domain.user.entity;

import static homes.banzzokee.domain.type.Role.SHELTER;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import homes.banzzokee.domain.common.entity.BaseEntity;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.LoginType;
import homes.banzzokee.domain.type.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 사용자
 */
@Entity
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  /**
   * 사용자 아이디
   */
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  /**
   * 이메일
   */
  private final String email;

  /**
   * 패스워드
   */
  private String password;

  /**
   * 닉네임
   */
  private String nickname;

  /**
   * 프로필 이미지 경로
   */
  private String profileImgUrl;

  /**
   * 자기소개
   */
  private String introduce;

  /**
   * 역할
   */
  @Enumerated(value = STRING)
  @JdbcTypeCode(SqlTypes.JSON)
  private Set<Role> role;

  /**
   * 로그인 유형
   */
  @Enumerated(value = STRING)
  private final LoginType loginType;

  /**
   * 보호소
   */
  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "shelter_id")
  private Shelter shelter;

  /**
   * 삭제일시
   */
  private LocalDateTime deletedAt;

  @Builder
  public User(String email, String password, String nickname, String profileImgUrl,
      String introduce, Set<Role> role, LoginType loginType, Shelter shelter) {
    this.email = email;
    this.password = password;
    this.nickname = nickname;
    this.profileImgUrl = profileImgUrl;
    this.introduce = introduce;
    this.role = role;
    this.loginType = loginType;
    this.shelter = shelter;
  }

  public boolean isWithdrawn() {
    return this.deletedAt != null;
  }

  public void withdraw() {
    if (this.deletedAt == null) {
      this.deletedAt = LocalDateTime.now();
    }
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
  }

  public boolean hasShelter() {
    return this.role.contains(SHELTER);
  }
}
