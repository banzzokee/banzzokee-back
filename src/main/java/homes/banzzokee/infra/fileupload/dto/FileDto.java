package homes.banzzokee.infra.fileupload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileDto {

  /**
   * 파일 저장된 경로
   */
  private final String url;

  /**
   * 변환된 파일명(조회 시 key로 사용됨)
   */
  private final String filename;
}