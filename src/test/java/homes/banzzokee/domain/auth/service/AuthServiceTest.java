package homes.banzzokee.domain.auth.service;

import homes.banzzokee.domain.auth.dto.EmailVerifyDto;
import homes.banzzokee.domain.auth.exception.EmailCodeInvalidException;
import homes.banzzokee.domain.auth.exception.EmailCodeUnmatchedException;
import homes.banzzokee.global.util.redis.RedisService;
import homes.banzzokee.domain.user.dao.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import homes.banzzokee.domain.auth.dto.EmailDto;
import homes.banzzokee.global.util.redis.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private RedisService redisService;

  @Captor
  private ArgumentCaptor<String> captor;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private AuthService authService;

  @Test
  @DisplayName("이메일 인증 테스트 - 성공 케이스")
  void successVerifyEmail() {
    // given
    String email = "test@test.com";
    String code = "123456";
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
    String email = "test@test.com";
    String code = "123456";
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
    String email = "test@test.com";
    String code = "123456";
    EmailVerifyDto emailVerifyDto = EmailVerifyDto.builder()
        .email(email)
        .code(code)
        .build();
    when(redisService.getData(email)).thenReturn(null);

    // when & then
    assertThrows(EmailCodeUnmatchedException.class, () -> authService.verifyEmail(emailVerifyDto));
  }

  @DisplayName("닉네임 중복확인 테스트 - 성공 케이스")
  void successCheckNickname() {
    // given
    String nickname = "반쪽이";
    when(userRepository.existsByNickname(nickname)).thenReturn(true);

    // when
    boolean result = !authService.checkNickname("반쪽이");

    // then
    assertTrue(result);
  }

  @Test
  @DisplayName("닉네임 중복확인 테스트 - 실패 케이스")
  void failCheckNickname() {
    // given
    String nickname = "반쪽이";
    when(userRepository.existsByNickname(nickname)).thenReturn(false);

    // when
    boolean result = authService.checkNickname("반쪽이");

    // then
    assertTrue(result);
  }

  @DisplayName("이메일 인증 발송 테스트")
  void successSendVerificationCode() {
    // given
    String email = "test@test.com";
    EmailDto emailDto = EmailDto.builder()
        .email(email)
        .build();

    // when
    authService.sendVerificationCode(emailDto);

    // then
    verify(redisService, times(1)).setData(captor.capture(), any(String.class), any(Long.class));
    assertEquals("test@test.com", captor.getValue());
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }
}