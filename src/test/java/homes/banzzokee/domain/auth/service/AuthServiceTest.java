package homes.banzzokee.domain.auth.service;

import homes.banzzokee.domain.auth.dto.EmailVerifyDto;
import homes.banzzokee.domain.auth.exception.EmailCodeInvalidException;
import homes.banzzokee.domain.auth.exception.EmailCodeUnmatchedException;
import homes.banzzokee.global.util.redis.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private RedisService redisService;

  @InjectMocks
  private AuthService authService;

  @Test
  @DisplayName("이메일 인증 테스트 - 성공 케이스")
  void successVerifyEmail() {
    // given
    String email = "test@test.com" ;
    String code = "123456" ;
    EmailVerifyDto emailVerifyDto = EmailVerifyDto.builder()
        .email("test@test.com")
        .code("123456")
        .build();

    when(redisService.getData(email)).thenReturn(code);

    // when
    assertDoesNotThrow(() -> authService.verifyEmail(emailVerifyDto));

    // then
    verify(redisService).deleteKey(email);
  }

  @Test
  @DisplayName("이메일 인증 테스트 - 실패 케이스 (코드 불일치)")
  void failVerifyEmailCodeUnmatched() {
    // given
    String email = "test@test.com" ;
    String code = "123456" ;
    EmailVerifyDto emailVerifyDto = EmailVerifyDto.builder()
        .email(email)
        .code(code)
        .build();
    when(redisService.getData(email)).thenReturn(email);

    // when & then
    assertThrows(EmailCodeInvalidException.class, () -> authService.verifyEmail(emailVerifyDto));
  }

  @Test
  @DisplayName("이메일 인증 테스트 - 실패 케이스 (코드 없음)")
  void failVerifyEmailCodeInvalid() {
    // given
    String email = "test@test.com" ;
    String code = "123456" ;
    EmailVerifyDto emailVerifyDto = EmailVerifyDto.builder()
        .email(email)
        .code(code)
        .build();
    when(redisService.getData(email)).thenReturn(null);

    // when & then
    assertThrows(EmailCodeUnmatchedException.class, () -> authService.verifyEmail(emailVerifyDto));
  }
}