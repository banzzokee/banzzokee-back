package homes.banzzokee.global.validator.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegexPattern {
  /**
   * 소문자, 대문자, 숫자, 특수문자 각각 최소 1개 이상을 포함하는 8자리 이상 20자리 이하
   */
  PASSWORD("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$"),
  ;

  private final String pattern;
}
