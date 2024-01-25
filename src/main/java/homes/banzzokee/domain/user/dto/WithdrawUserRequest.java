package homes.banzzokee.domain.user.dto;

import homes.banzzokee.global.validator.annotation.Password;

/**
 * 회원 탈퇴 요청
 *
 * @param password 패스워드
 */
public record WithdrawUserRequest(
    @Password String password) {

}
