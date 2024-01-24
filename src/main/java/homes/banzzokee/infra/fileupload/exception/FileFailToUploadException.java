package homes.banzzokee.infra.fileupload.exception;

import homes.banzzokee.global.error.ErrorCode;
import homes.banzzokee.global.error.exception.CustomException;
import lombok.Getter;

@Getter
public class FileFailToUploadException extends CustomException {

  public FileFailToUploadException() {
    super(ErrorCode.FAIL_TO_UPLOAD_FILE);
  }

}