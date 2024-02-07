package homes.banzzokee.domain.auth.service;

import homes.banzzokee.domain.auth.dto.*;
import homes.banzzokee.domain.auth.exception.*;
import homes.banzzokee.domain.type.LoginType;
import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.security.jwt.JwtTokenProvider;
import homes.banzzokee.global.util.redis.RedisService;
import homes.banzzokee.domain.user.dao.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private RedisService redisService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @InjectMocks
  private AuthService authService;

  @Test
  @DisplayName("[이메일 인증 코드] - 성공 검증")
  void verifyEmailCode_when_success_then_verify() {
    // given
    String email = "test@test.com";
    String code = "123456";
    EmailVerifyRequest emailVerifyRequest = EmailVerifyRequest.builder()
        .email("test@test.com")
        .code("123456")
        .build();

    when(redisService.getData(email)).thenReturn(code);

    // when
    assertDoesNotThrow(() -> authService.verifyEmail(emailVerifyRequest));

    // then
    verify(redisService).deleteKey(email);
  }

  @Test
  @DisplayName("[이메일 인증 코드] - 이메일로 전송된 인증 코드가 유효하지 않은 경우 EmailCodeInvalidException 발생")
  void verifyEmailCode_when_verifyCode_then_EmailCodeInvalidException() {
    // given
    String email = "test@test.com";
    String code = "123456";
    EmailVerifyRequest emailVerifyRequest = EmailVerifyRequest.builder()
        .email(email)
        .code(code)
        .build();
    when(redisService.getData(email)).thenReturn(email);

    // when & then
    assertThrows(EmailCodeInvalidException.class, () -> authService.verifyEmail(emailVerifyRequest));
  }

  @Test
  @DisplayName("[이메일 인증] - 해당 이메일로 전송된 코드가 없는 경우 EmailUnmatchedException 발생")
  void verifyEmailCode_when_verifyEmail_them_EmailUnmatchedException() {
    // given
    String email = "test@test.com";
    String code = "123456";
    EmailVerifyRequest emailVerifyRequest = EmailVerifyRequest.builder()
        .email(email)
        .code(code)
        .build();
    when(redisService.getData(email)).thenReturn(null);

    // when & then
    assertThrows(EmailUnmatchedException.class, () -> authService.verifyEmail(emailVerifyRequest));
  }

  @Test
  @DisplayName("[닉네임 중복 확인] - 성공 검증")
  void checkNickname_when_success_then_verify() {
    // given
    String nickname = "반쪽이";
    when(userRepository.existsByNicknameAndDeletedAtIsNull(nickname)).thenReturn(true);

    // when
    boolean result = !authService.checkNickname("반쪽이");

    // then
    assertTrue(result);
  }

  @Test
  @DisplayName("[이메일 전송 확인] - 성공 검증")
  void sendVerificationCode_when_success_then_verify() {
    // given
    EmailRequest emailRequest = EmailRequest.builder()
        .email("test@test.com")
        .build();
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(emailRequest.getEmail());
    message.setSubject("인증코드입니다.");
    message.setText(String.format("인증코드는: %s", "123456"));

    // when
    doNothing().when(mailSender).send(any(SimpleMailMessage.class));
    doNothing().when(redisService).setData(anyString(), anyString(), anyLong());
    authService.sendVerificationCode(emailRequest);

    // then
    verify(redisService).setData(anyString(), anyString(), anyLong());
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("[회원가입] - 성공 검증")
  void signup_when_success_then_verify() {
    // given
    SignupRequest signupRequest = SignupRequest.builder()
        .email("test@gmail.com")
        .password("Password123!")
        .confirmPassword("Password123!")
        .nickname("test")
        .build();
    User expectedUser = User.builder()
        .email(signupRequest.getEmail())
        .password("EncodedPassword123!")
        .nickname(signupRequest.getNickname())
        .build();

    given(userRepository.existsByEmailAndDeletedAtIsNull(signupRequest.getEmail())).willReturn(false);
    given(userRepository.existsByNicknameAndDeletedAtIsNull(signupRequest.getNickname())).willReturn(false);
    given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("EncodedPassword123!");

    // when
    authService.signup(signupRequest);

    // then
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User savedUser = captor.getValue();
    assertEquals(expectedUser.getEmail(), savedUser.getEmail());
    assertEquals(expectedUser.getPassword(), savedUser.getPassword());
    assertEquals(expectedUser.getNickname(), savedUser.getNickname());
  }

  @Test
  @DisplayName("[회원가입] - 이메일 중복인 경우 EmailDuplicatedException 발생")
  void signup_when_verifyEmail_then_EmailDuplicatedException() {
    // given
    SignupRequest signupRequest = SignupRequest.builder()
        .email("test@gmail.com")
        .password("Password123!")
        .confirmPassword("Password123!")
        .nickname("test")
        .build();

    given(userRepository.existsByEmailAndDeletedAtIsNull(signupRequest.getEmail())).willReturn(true);

    // when & then
    assertThrows(EmailDuplicatedException.class, () -> authService.signup(signupRequest));
  }

  @Test
  @DisplayName("[회원가입] - 닉네임 중복인 경우 NicknameDuplicatedException 발생")
  void signup_when_verifyNickname_then_NicknameDuplicatedException() {
    // given
    SignupRequest signupRequest = SignupRequest.builder()
        .email("test@gmail.com")
        .password("Password123!")
        .confirmPassword("Password123!")
        .nickname("test")
        .build();

    given(userRepository.existsByNicknameAndDeletedAtIsNull(signupRequest.getNickname())).willReturn(true);

    // when & then
    assertThrows(NicknameDuplicatedException.class, () -> authService.signup(signupRequest));
  }

  @Test
  @DisplayName("[회원가입] - 패스워드가 다른 경우 ConfirmPasswordUnMatchException 발생")
  void signup_when_verifyConfirmPassword_then_ConfirmPasswordUnMatchException() {
    // given
    SignupRequest signupRequest = SignupRequest.builder()
        .email("test@gmail.com")
        .password("Password123!")
        .confirmPassword("TestPassword123!")
        .nickname("test")
        .build();

    // when & then
    assertThrows(ConfirmPasswordUnMatchException.class, () -> authService.signup(signupRequest));
  }

  @Test
  @DisplayName("[이메일 로그인] - 성공 검증")
  void signIn_when_success_then_verify() {
    // given
    SignInRequest signInRequest = SignInRequest.builder()
        .email("test@test.com")
        .password("@@Banzzokee12345")
        .build();

    given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
    User user = User.builder()
        .email("test@test.com")
        .password(passwordEncoder.encode("@@Banzzokee12345"))
        .nickname("test")
        .role(Set.of(Role.ROLE_USER))
        .loginType(LoginType.EMAIL)
        .build();

    TokenResponse tokenResponse = TokenResponse.builder()
        .accessToken("Bearer!@#$%^&*()_+1234567890")
        .refreshToken("Bearer1234567890!@#$%^&*()")
        .build();

    given(userRepository.findByEmail(signInRequest.getEmail())).willReturn(Optional.of(user));
    given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
    given(jwtTokenProvider.createAccessToken(any(User.class))).willReturn(tokenResponse.getAccessToken());
    given(jwtTokenProvider.createRefreshToken(any(User.class))).willReturn(tokenResponse.getRefreshToken());

    // when
    TokenResponse result = authService.signIn(signInRequest);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo(tokenResponse.getAccessToken());
    assertThat(result.getRefreshToken()).isEqualTo(tokenResponse.getRefreshToken());
  }

  @Test
  @DisplayName("[이메일 로그인] - 이메일이 존재하지 않을 경우 EmailNotFoundException 발생")
  void signIn_when_verifyEmail_then_throw_EmailNotFoundException() {
    // given
    SignInRequest signInRequest = SignInRequest.builder()
        .email("test@test.com")
        .password("@@Banzzokee12345")
        .build();

    given(userRepository.findByEmail(signInRequest.getEmail())).willReturn(Optional.empty());

    // when & then
    assertThrows(EmailNotFoundException.class, () -> authService.signIn(signInRequest));
  }

  @Test
  @DisplayName("[이메일 로그인] - 패스워드가 일치하지 않는 경우 PasswordUnmatchedException 발생")
  void signIn_when_verifyPassword_then_PasswordUnmatchedException() {
    // given
    SignInRequest signInRequest = SignInRequest.builder()
        .email("test@test.com")
        .password("@@Banzzokee12345")
        .build();
    User user = User.builder()
        .email("test@test.com")
        .password("UnmatchedPassword")
        .build();

    given(userRepository.findByEmail(signInRequest.getEmail())).willReturn(Optional.of(user));
    given(passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())).willReturn(false);

    // then & then
    assertThrows(PasswordUnmatchedException.class, () -> authService.signIn(signInRequest));
  }
}