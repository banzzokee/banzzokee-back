package homes.banzzokee.global.validator;

import homes.banzzokee.domain.adoption.dto.AdoptionStatusChangeRequest;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.global.validator.annotation.StatusRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StatusRequestValidator implements
    ConstraintValidator<StatusRequest, AdoptionStatusChangeRequest> {

  @Override
  public boolean isValid(AdoptionStatusChangeRequest value,
      ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();

    if (value == null) {
      context.buildConstraintViolationWithTemplate(
              "유효한 상태변경 요청이 아닙니다.")
          .addConstraintViolation();
      return false;
    }

    if (value.getStatus().equals(AdoptionStatus.FINISHED.getStatus())
        && value.getAssignedUserId() == null) {
      context.buildConstraintViolationWithTemplate(
              "분양완료로 상태변경하기 위해서는 입양자 정보가 필요합니다.")
          .addConstraintViolation();
      return false;
    }

    if (value.getStatus().equals(AdoptionStatus.RESERVING.getStatus())
        || value.getStatus().equals(AdoptionStatus.ADOPTING.getStatus())) {
      if (value.getAssignedUserId() != null) {
        context.buildConstraintViolationWithTemplate(
                "분양중 또는 예약중으로 상태변경하기 위해서는 입양자 정보가 필요없습니다.")
            .addConstraintViolation();
        return false;
      }
    }

    return true;
  }
}
