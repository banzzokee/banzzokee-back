package homes.banzzokee.domain.common.image.exception;

import homes.banzzokee.global.error.ErrorCode;
import homes.banzzokee.global.error.exception.CustomException;
import lombok.Getter;

@Getter
public class ImageFailToUploadException extends CustomException {

  public ImageFailToUploadException(ErrorCode errorCode) {
    super(errorCode);
  }

}
