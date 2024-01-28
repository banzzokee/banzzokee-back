package homes.banzzokee.domain.type;

import homes.banzzokee.infra.fileupload.dto.ImageDto;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * S3 Object 임베디드 타입
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class S3Object {

  /**
   * 경로
   */
  private String url;

  public S3Object(String url) {
    this.url = url;
  }

  /**
   * @return Object Key
   */
  public String getFileName() {
    return StringUtils.getFilename(url);
  }

  public static S3Object from(ImageDto image) {
    return (image != null) ? new S3Object(image.getUrl()) : null;
  }
}
