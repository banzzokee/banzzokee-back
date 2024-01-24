package homes.banzzokee.domain.auth.service;

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

  @InjectMocks
  private AuthService authService;

  @Test
  @DisplayName("이메일 인증 발송 테스트")
  void successSendVerificationCode() {
    // given
    String email = "test@test.com" ;
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