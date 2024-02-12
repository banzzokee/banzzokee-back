package homes.banzzokee.domain.auth.service;

import static homes.banzzokee.domain.type.Role.ROLE_USER;

import homes.banzzokee.domain.auth.dto.EmailRequest;
import homes.banzzokee.domain.auth.dto.EmailVerifyRequest;
import homes.banzzokee.domain.auth.dto.SignInRequest;
import homes.banzzokee.domain.auth.dto.SignupRequest;
import homes.banzzokee.domain.auth.dto.TokenResponse;
import homes.banzzokee.domain.auth.exception.ConfirmPasswordUnMatchException;
import homes.banzzokee.domain.auth.exception.EmailCodeInvalidException;
import homes.banzzokee.domain.auth.exception.EmailDuplicatedException;
import homes.banzzokee.domain.auth.exception.EmailNotFoundException;
import homes.banzzokee.domain.auth.exception.EmailUnmatchedException;
import homes.banzzokee.domain.auth.exception.NicknameDuplicatedException;
import homes.banzzokee.domain.auth.exception.PasswordUnmatchedException;
import homes.banzzokee.domain.type.LoginType;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.security.jwt.JwtTokenProvider;
import homes.banzzokee.global.util.redis.RedisService;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private static final String EMAIL_SUBJECT = "인증 코드";
  private static final String EMAIL_TEXT = "인증 코드는 %s 입니다.";
  private static final int VERIFICATION_CODE_EXPIRATION_TIME = 3;
  private static final int VERIFICATION_CODE_MIN_VALUE = 100000;
  private static final int VERIFICATION_CODE_MAX_VALUE = 1000000;

  private final RedisService redisService;
  private final JavaMailSender mailSender;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public void signup(SignupRequest signupRequest) {
    if (userRepository.existsByEmailAndDeletedAtIsNull(signupRequest.getEmail())) {
      throw new EmailDuplicatedException();
    }
    String confirmPassword = signupRequest.getConfirmPassword();
    if (!signupRequest.getPassword().equals(confirmPassword)) {
      throw new ConfirmPasswordUnMatchException();
    }
    if (userRepository.existsByNicknameAndDeletedAtIsNull(signupRequest.getNickname())) {
      throw new NicknameDuplicatedException();
    }
    userRepository.save(User.builder()
        .email(signupRequest.getEmail())
        .password(passwordEncoder.encode(signupRequest.getPassword()))
        .nickname(signupRequest.getNickname())
        .role(Set.of(ROLE_USER))
        .loginType(LoginType.EMAIL)
        .build());
  }

  public void sendVerificationCode(EmailRequest emailRequest) {
    String code = generateVerificationCode();
    redisService.setData(emailRequest.getEmail(), code, Duration.ofMinutes(VERIFICATION_CODE_EXPIRATION_TIME).getSeconds());
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(emailRequest.getEmail());
    message.setSubject(EMAIL_SUBJECT);
    message.setText(String.format(EMAIL_TEXT, code));
    mailSender.send(message);
  }

  private String generateVerificationCode() {
    return String.valueOf(ThreadLocalRandom.current()
        .nextInt(VERIFICATION_CODE_MIN_VALUE, VERIFICATION_CODE_MAX_VALUE));
  }

  public void verifyEmail(EmailVerifyRequest emailVerifyRequest) {
    String email = emailVerifyRequest.getEmail();
    String code = emailVerifyRequest.getCode();
    String redisCode = redisService.getData(email);
    if (redisCode == null) {
      throw new EmailUnmatchedException();
    }
    if (!code.equals(redisCode)) {
      throw new EmailCodeInvalidException();
    }
    redisService.deleteKey(email);
  }

  public boolean checkNickname(String nickname) {
    return !userRepository.existsByNicknameAndDeletedAtIsNull(nickname);
  }

  @Transactional
  public TokenResponse signIn(SignInRequest signInRequest) {
    String email = signInRequest.getEmail();
    String password = signInRequest.getPassword();
    User user = userRepository.findByEmail(email)
        .orElseThrow(EmailNotFoundException::new);
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new PasswordUnmatchedException();
    }
    String accessToken = jwtTokenProvider.createAccessToken(user);
    String refreshToken = jwtTokenProvider.createRefreshToken(user);
    redisService.setRefreshToken(user.getEmail(), refreshToken, jwtTokenProvider.getRefreshTokenExpire());
    return TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  /**
   * 로그아웃
   *
   * @param token
   */
  public void logout(String token) {
    redisService.addToBlacklist(token);
    redisService.deleteRefreshToken(token);
  }
}