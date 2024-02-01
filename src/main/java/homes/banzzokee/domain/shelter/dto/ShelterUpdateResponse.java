package homes.banzzokee.domain.shelter.dto;

import homes.banzzokee.domain.shelter.entity.Shelter;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * 보호소 수정 응답
 */
@Getter
@Builder
public class ShelterUpdateResponse {

  /**
   * 보호소 아이디
   */
  private final Long shelterId;

  /**
   * 이름
   */
  private final String name;

  /**
   * 설명
   */
  private final String description;

  /**
   * 연락처
   */
  private final String tel;

  /**
   * 이미지 경로
   */
  private final String shelterImgUrl;

  /**
   * 주소
   */
  private final String address;

  /**
   * 위도
   */
  private final Double latitude;

  /**
   * 경도
   */
  private final Double longitude;

  /**
   * 수정일시
   */
  private final LocalDateTime updatedAt;

  public static ShelterUpdateResponse fromEntity(Shelter shelter) {
    return ShelterUpdateResponse.builder()
        .shelterId(shelter.getId())
        .name(shelter.getName())
        .description(shelter.getDescription())
        .tel(shelter.getTel())
        .shelterImgUrl(shelter.getShelterImageUrl())
        .address(shelter.getAddress())
        .latitude(shelter.getLatitude())
        .longitude(shelter.getLongitude())
        .updatedAt(shelter.getUpdatedAt())
        .build();
  }
}
