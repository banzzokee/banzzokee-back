package homes.banzzokee.domain.common.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageDto {

  /**
   * 이미지 파일 저장된 경로
   */
  private final String url;

  /**
   * 변환된 파일명(조회 시 key로 사용됨)
   */
  private final String filename;
}