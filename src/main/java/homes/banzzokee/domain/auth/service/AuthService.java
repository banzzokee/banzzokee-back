package homes.banzzokee.domain.auth.service;

import homes.banzzokee.domain.auth.dto.EmailDto;
import homes.banzzokee.domain.auth.dto.EmailVerifyDto;
import homes.banzzokee.domain.auth.dto.SignupDto;
import homes.banzzokee.domain.auth.exception.*;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.util.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  private final RedisService redisService;
  private final JavaMailSender mailSender;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void signup(SignupDto signupDto) {
    if (userRepository.existsByEmail(signupDto.getEmail())) {
      throw new EmailDuplicatedException();
    }
    String confirmPassword = signupDto.getConfirmPassword();
    if (!signupDto.getPassword().equals(confirmPassword)) {
      throw new ConfirmPasswordUnMatchException();
    }
    if (userRepository.existsByNickname(signupDto.getNickname())) {
      throw new NicknameDuplicatedException();
    }
    User user = signupDto.toEntity();
    user.setPassword(passwordEncoder.encode(signupDto.getPassword()));
    userRepository.save(user);
  }

  public void sendVerificationCode(EmailDto emailDto) {
    String code = generateVerificationCode();
    redisService.setData(emailDto.email(), code, Duration.ofMinutes(VERIFICATION_CODE_EXPIRATION_TIME).getSeconds());
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(emailDto.email());
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