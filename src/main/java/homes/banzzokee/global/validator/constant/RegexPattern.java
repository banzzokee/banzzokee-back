package homes.banzzokee.global.validator.constant;

public final class RegexPattern {

  /**
   * 공백 처리와 올바른 이메일 형태로 요청 받기 위해 생성
   * 소문자, 대문자, 숫자, 특수문자 각각 최소 1개 이상을 포함하는 8자리 이상 20자리 이하
   */

  public static final String EMAIL = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
  public static final String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$";
  public static final String TEL = "^(01[016789]{1}|02|0[3-9]{1}[0-9]{1})-?[0-9]{3,4}-?[0-9]{4}$";
}
