package homes.banzzokee.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 사용자 프로필 수정 요청
 *
 * @param nickname  닉네임
 * @param introduce 자기소개
 */
public record UpdateUserRequest(
    @NotBlank String nickname,
    @NotBlank String introduce
) {

}
