package homes.banzzokee.domain.shelter.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보호소
 */
@Entity
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
  private String shelterImgUrl;

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

  @Builder
  public Shelter(String name, String description, String shelterImgUrl, String tel,
      String address,
      Double latitude, Double longitude, boolean verified) {
    this.name = name;
    this.description = description;
    this.shelterImgUrl = shelterImgUrl;
    this.tel = tel;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
    this.verified = verified;
  }
}
