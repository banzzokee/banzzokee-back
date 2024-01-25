package homes.banzzokee.domain.user.dto;

import homes.banzzokee.global.validator.annotation.Password;
import lombok.Builder;

/**
 * 패스워드 변경 요청
 *
 * @param originPassword  원래 패스워드
 * @param newPassword     변경할 패스워드
 * @param confirmPassword 패스워드 확인
 */
@Builder
public record ChangePasswordRequest(
    @Password String originPassword,
    @Password String newPassword,
    @Password String confirmPassword
) {

}
