package homes.banzzokee.domain.shelter.entity;

import static homes.banzzokee.domain.type.Role.SHELTER;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.common.entity.BaseEntity;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.StringUtils;

/**
 * 보호소
 */
@Entity
@DynamicUpdate
@Getter
@NoArgsConstructor(force = true, access = PROTECTED)
public class Shelter extends BaseEntity {

  /**
   * 보호소 아이디
   */
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  /**
   * 이름
   */
  private String name;

  /**
   * 설명
   */
  private String description;

  /**
   * 이미지 경로
   */
  @Embedded
  @AttributeOverrides(
      @AttributeOverride(name = "url", column = @Column(name = "shelterImgUrl"))
  )
  private S3Object shelterImage;

  /**
   * 연락처
   */
  private String tel;

  /**
   * 주소
   */
  private String address;

  /**
   * 위도
   */
  private Double latitude;

  /**
   * 경도
   */
  private Double longitude;

  /**
   * 승인여부
   */
  private boolean verified;

  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "user_id")
  private final User user;

  @Builder
  public Shelter(String name, String description, String shelterImgUrl, String tel,
      String address,
      Double latitude, Double longitude, boolean verified, User user) {
    this.name = name;
    this.description = description;
    this.tel = tel;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
    this.verified = verified;
    this.user = Objects.requireNonNull(user);

    if (StringUtils.hasText(shelterImgUrl)) {
      this.shelterImage = new S3Object(shelterImgUrl);
    }
  }

  public String getShelterImageUrl() {
    return this.getShelterImage() != null ? this.getShelterImage().getUrl() : null;
  }

  public void verify() {
    this.verified = true;
    assert this.user != null;
    this.user.addRoles(SHELTER);
  }

  public void updateProfile(String name, String description, String tel, String address,
      Double latitude, Double longitude, S3Object shelterImage) {
    this.name = name;
    this.description = description;
    this.tel = tel;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
    if (shelterImage != null) {
      this.shelterImage = shelterImage;
    }
  }
}
