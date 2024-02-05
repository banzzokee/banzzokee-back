package homes.banzzokee.global.validator;

import homes.banzzokee.global.validator.annotation.FileDuplicateValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

/**
 * 여러 개 파일을 동시에 업로드 할 경우 중복된 파일 존재 여부 확인
 */
public class FileDuplicateValidator implements ConstraintValidator<FileDuplicateValid, List<MultipartFile>> {

  @Override
  public boolean isValid(List<MultipartFile> value, ConstraintValidatorContext context) {
    Set<String> duplicatFileCheckSet = new HashSet<>();
    for (MultipartFile multipartFile : value) {
      if (!duplicatFileCheckSet.add(multipartFile.getOriginalFilename())) {
        return false;
      }
    }
    return true;
  }
}
