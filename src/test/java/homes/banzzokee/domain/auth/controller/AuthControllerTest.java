package homes.banzzokee.domain.auth.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import homes.banzzokee.domain.auth.dto.*;
import homes.banzzokee.domain.auth.service.AuthService;
import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AuthService authService;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("[이메일 전송] - 성공 검증")
  void sendVerificationCode_validInput_then_success() throws Exception {
    // given
    String email = "test@test.com";
    EmailRequest emailRequest = EmailRequest.builder()
        .email(email)
        .build();
    String requestBody = objectMapper.writeValueAsString(emailRequest);

    // when
    mockMvc.perform(post("/api/auth/send-verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk());

    // then
    verify(authService).sendVerificationCode(any(EmailRequest.class));
  }

  @Test
  @DisplayName("[이메일 인증] - 성공 검증")
  void verifyEmail_when_validInput_then_success() throws Exception {
    // given
    EmailVerifyRequest emailVerifyRequest = EmailVerifyRequest.builder()
        .email("test@test.com")
        .code("123456")
        .build();
    String requestBody = objectMapper.writeValueAsString(emailVerifyRequest);

    // when & then
    mockMvc.perform(post("/api/auth/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk());

    verify(authService).verifyEmail(any(EmailVerifyRequest.class));
  }

  @Test
  @WithMockUser
  @DisplayName("[닉네임 중복 확인] - 성공 검증")
  void checkNickname_when_validInput_then_success() throws Exception {
    // given
    when(authService.checkNickname("반쪽이")).thenReturn(true);

    // when & then
    mockMvc.perform(get("/api/auth/nickname-check")
            .param("nickname", "반쪽이"))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));

    verify(authService).checkNickname("반쪽이");
  }

  @Test
  @WithMockUser
  @DisplayName("[닉네임 중복 확인] - 실패 검증")
  void checkNickname_when_validInput_then_fail() throws Exception {
    //given
    when(authService.checkNickname("반쪽이")).thenReturn(false);

    //when & then
    mockMvc.perform(get("/api/auth/nickname-check")
            .param("nickname", "반쪽이"))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));

    verify(authService).checkNickname("반쪽이");
  }

  @Test
  @DisplayName("[자사 회원가입] - 성공 검증")
  void signup_when_validInput_then_success() throws Exception {
    // given
    SignupRequest signupRequest = SignupRequest.builder()
        .email("test@gmail.com")
        .password("Password123!")
        .confirmPassword("Password123!")
        .nickname("test")
        .build();
    doNothing().when(authService).signup(any(SignupRequest.class));

    // when & then
    mockMvc.perform(post("/api/auth/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isOk());

    verify(authService).signup(any(SignupRequest.class));
  }

  @Test
  @WithMockUser
  @DisplayName("[이메일 로그인] - 성공 검증")
  void signIn_when_validInput_then_success() throws Exception {
    // given
    SignInRequest signInRequest = SignInRequest.builder()
        .email("test@test.com")
        .password("@@Banzzokee12345")
        .build();
    TokenResponse tokenResponse = TokenResponse.builder()
        .accessToken("!@#$%^&*()1234567890")
        .refreshToken("1234567890)(*&^%$#@!")
        .build();
    given(authService.signIn(any(SignInRequest.class))).willReturn(tokenResponse);

    // when & then
    mockMvc.perform(post("/api/auth/sign-in")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signInRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("!@#$%^&*()1234567890"))
        .andExpect(jsonPath("$.refreshToken").value("1234567890)(*&^%$#@!"));
  }

  @Test
  @WithMockUser
  @DisplayName("[로그아웃] - 성공 검증")
  void logout_when_valid_then_success() throws Exception {
    // given
    String token = "Bearer testToken";

    // when
    doNothing().when(authService).logout(token);

    // then
    mockMvc.perform(post("/api/auth/logout")
            .header("Authorization", token))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  @DisplayName("[토큰 재발급] - 성공 검증")
  void tokenReissue_when_validInput_then_success() throws Exception {
    // given
    String refreshToken = "Bearer refreshToken";
    TokenResponse tokenResponse = new TokenResponse(
        "newAccessToken", "refreshToken");
    given(authService.reissueAccessToken(anyString())).willReturn(tokenResponse);

    // when & then
    mockMvc.perform(post("/api/auth/token/reissue")
            .header("Authorization", refreshToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
        .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    verify(authService).reissueAccessToken(refreshToken);
  }
}
