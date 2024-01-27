package homes.banzzokee.global.validator;

import static homes.banzzokee.global.error.ErrorCode.MIME_TYPE_INVALID;

import homes.banzzokee.global.error.exception.CustomException;
import homes.banzzokee.global.validator.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.util.Arrays;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

  private final Tika tika = new Tika();

  private ValidFile validFile;

  @Override
  public void initialize(ValidFile validFile) {
    this.validFile = validFile;
  }

  @Override
  public boolean isValid(MultipartFile value,
      ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();

    if (value == null) {
      return true;
    }

    String mimeType = getMimeType(value);
    boolean matched = Arrays.stream(validFile.whiteList())
        .anyMatch(o -> o.equalsIgnoreCase(mimeType));
    if (!matched) {
      addMessage(context, validFile.message());
    }

    return matched;
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
