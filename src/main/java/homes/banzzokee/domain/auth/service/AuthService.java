package homes.banzzokee.domain.auth.service;

import homes.banzzokee.domain.auth.dto.EmailDto;
import homes.banzzokee.domain.auth.dto.EmailVerifyDto;
import homes.banzzokee.domain.auth.dto.SignupDto;
import homes.banzzokee.domain.auth.exception.EmailCodeInvalidException;
import homes.banzzokee.domain.auth.exception.EmailCodeUnmatchedException;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.global.util.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private static final String EMAIL_SUBJECT = "인증 코드";
  private static final String EMAIL_TEXT = "인증 코드는 %s 입니다.";
  private static final int VERIFICATION_CODE_EXPIRATION_TIME = 3;
  private static final int VERIFICATION_CODE_MIN_VALUE = 100000;
  private static final int VERIFICATION_CODE_MAX_VALUE = 900000;

  private final JavaMailSender mailSender;
  private final RedisService redisService;

  private final UserRepository userRepository;

  public void signup(SignupDto signupDto) {

  }

  public void sendVerificationCode(EmailDto emailDto) {
    String code = generateVerificationCode();
    redisService.setData(emailDto.getEmail(), code, Duration.ofMinutes(VERIFICATION_CODE_EXPIRATION_TIME).getSeconds());
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(emailDto.getEmail());
    message.setSubject(EMAIL_SUBJECT);
    message.setText(String.format(EMAIL_TEXT, code));
    mailSender.send(message);
  }

  private String generateVerificationCode() {
    return String.valueOf(VERIFICATION_CODE_MIN_VALUE + new Random().nextInt(VERIFICATION_CODE_MAX_VALUE));
  }

  public void verifyEmail(EmailVerifyDto emailVerifyDto) {
    String email = emailVerifyDto.getEmail();
    String code = emailVerifyDto.getCode();
    String redisCode = (String) redisService.getData(email);
    if (redisCode == null) {
      throw new EmailCodeUnmatchedException();
    }
    if (!code.equals(redisCode)) {
      throw new EmailCodeInvalidException();
    }
    redisService.deleteKey(email);
  }

  public boolean checkNickname(String nickname) {
    return !userRepository.existsByNickname(nickname);
  }
}