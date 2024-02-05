package homes.banzzokee.global.validator;

import static homes.banzzokee.global.error.ErrorCode.MIME_TYPE_INVALID;

import homes.banzzokee.global.error.exception.CustomException;
import homes.banzzokee.global.validator.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<ValidFile, Object> {

  private final Tika tika = new Tika();

  private ValidFile validFile;

  @Override
  public void initialize(ValidFile validFile) {
    this.validFile = validFile;
  }

  @Override
  public boolean isValid(Object value,
      ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();

    if (value == null) {
      return true;
    }

    if (value instanceof List<?> filelist) {
      for (Object file : filelist) {
        if (!(file instanceof MultipartFile)) {
          throw new IllegalArgumentException(
              "유효한 객체 타입이 아닙니다. List<MultipartFile> 또는 MultipartFile만 적용 가능합니다.");
        }
        if (!isValidFile((MultipartFile) file)) {
          addMessage(context, validFile.message());
          return false;
        }
      }
      return true;
    } else if (value instanceof MultipartFile) {
      if (isValidFile((MultipartFile) value)) {
        return true;
      } else {
        addMessage(context, validFile.message());
        return false;
      }
    }

    throw new IllegalArgumentException(
        "유효한 객체 타입이 아닙니다. List<MultipartFile> 또는 MultipartFile만 적용 가능합니다.");

  }


  private boolean isValidFile(MultipartFile file) {

    String mimeType = getMimeType(file);

    return Arrays.stream(validFile.whiteList())
        .anyMatch(o -> o.equalsIgnoreCase(mimeType));
  }

  private String getMimeType(MultipartFile value) {
    try {
      return tika.detect(value.getInputStream());
    } catch (IOException e) {
      throw new CustomException(MIME_TYPE_INVALID);
    }
  }

  private void addMessage(ConstraintValidatorContext context, String msg) {
    context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
  }
}
