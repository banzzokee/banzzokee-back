package homes.banzzokee.global.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
class PasswordValidatorTest {

  private final String VALID_PASSWORD = "1q2W#e$R";

  @Test
  @DisplayName("비밀번호 유효성 검사 통과")
  void successPasswordValidator() {
    // given
    // when
    // then
    PasswordValidator validator = new PasswordValidator();
    assertTrue(validator.isValid(VALID_PASSWORD, null));
    log.info("비밀번호 유효성 통과: {}", VALID_PASSWORD);
  }

  @Test
  @DisplayName("비밀번호 유효성 검사 실패 - 숫자 미포함")
  void failPasswordValidator_Not_Contains_Number() {
    // given
    String password = VALID_PASSWORD.replaceAll("\\d", "");
    // when
    // then
    PasswordValidator validator = new PasswordValidator();
    assertFalse(validator.isValid(password, null));
    log.info("비밀번호 숫자 미포함: {}", password);
  }

  @Test
  @DisplayName("비밀번호 유효성 검사 실패 - 소문자 미포함")
  void failPasswordValidator_Not_Contains_Lowercase() {
    // given
    String password = VALID_PASSWORD.replaceAll("[a-z]", "");
    // when
    // then
    PasswordValidator validator = new PasswordValidator();
    assertFalse(validator.isValid(password, null));
    log.info("비밀번호 소문자 미포함: {}", password);
  }

  @Test
  @DisplayName("비밀번호 유효성 검사 실패 - 대문자 미포함")
  void failPasswordValidator_Not_Contains_Uppercase() {
    // given
    String password = VALID_PASSWORD.replaceAll("[A-Z]", "");
    // when
    // then
    PasswordValidator validator = new PasswordValidator();
    assertFalse(validator.isValid(password, null));
    log.info("비밀번호 대문자 미포함: {}", password);
  }

  @Test
  @DisplayName("비밀번호 유효성 검사 실패 - 특수문자")
  void failPasswordValidator_Not_Contains_SpecialCharacter() {
    // given
    String password = VALID_PASSWORD.replaceAll("[^a-zA-Z0-9]", "");
    // when
    // then
    PasswordValidator validator = new PasswordValidator();
    assertFalse(validator.isValid(password, null));
    log.info("비밀번호 특수문자 미포함: {}", password);
  }

  @Test
  @DisplayName("비밀번호 유효성 검사 실패 - 8자리 이하")
  void failPasswordValidator_Length_Below_8() {
    // given
    String password = VALID_PASSWORD.substring(0, 7);
    // when
    // then
    PasswordValidator validator = new PasswordValidator();
    assertFalse(validator.isValid(password, null));
    log.info("비밀번호 8자리 이하: {}", password);
  }

  @Test
  @DisplayName("비밀번호 유효성 검사 실패 - 20자리 초과")
  void failPasswordValidator_Length_Above_20() {
    // given
    String password = VALID_PASSWORD
        .concat(VALID_PASSWORD)
        .concat(VALID_PASSWORD)
        .substring(0, 21);
    // when
    // then
    PasswordValidator validator = new PasswordValidator();
    assertFalse(validator.isValid(password, null));
    log.info("비밀번호 20자리 초과: {}", password);
  }
}