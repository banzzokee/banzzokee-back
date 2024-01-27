package homes.banzzokee.domain.shelter.dto;

import static homes.banzzokee.global.validator.constant.RegexPattern.TEL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 보호소 수정 요청
 */
@Getter
@Builder
@EqualsAndHashCode
public class ShelterUpdateRequest {

  /**
   * 이름
   */
  @NotBlank
  private final String name;

  /**
   * 주소
   */
  @NotBlank
  private final String address;

  /**
   * 설명
   */
  @NotBlank
  private final String description;

  /**
   * 연락처
   */
  @Pattern(regexp = TEL)
  private final String tel;

  /**
   * 위도
   */
  @NotNull
  private final Double latitude;

  /**
   * 경도
   */
  @NotNull
  private final Double longitude;
}
