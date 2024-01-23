package homes.banzzokee.domain.auth.service;

import homes.banzzokee.domain.auth.dto.EmailDto;
import homes.banzzokee.domain.auth.dto.SignupDto;
import homes.banzzokee.domain.user.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final String EMAIL_SUBJECT = "인증 코드";
  private static final String EMAIL_TEXT = "인증 코드는 %s 입니다.";

  private JavaMailSender mailSender;
  private StringRedisTemplate redisTemplate;

  private final UserRepository userRepository;

  public void signup(SignupDto signupDto) {

  }

  public void sendVerificationCode(EmailDto emailDto) {
    String code = generateVerificationCode();
    redisTemplate.opsForValue().set(emailDto.getEmail(), code, Duration.ofMinutes(3));
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(emailDto.getEmail());
    message.setSubject(EMAIL_SUBJECT);
    message.setText(String.format(EMAIL_TEXT, code));
    mailSender.send(message);
  }

  private String generateVerificationCode() {
    return String.valueOf(100000 + new Random().nextInt(900000));
  }

}
