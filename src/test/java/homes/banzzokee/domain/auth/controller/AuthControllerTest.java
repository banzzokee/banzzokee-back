//package homes.banzzokee.domain.auth.controller;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import homes.banzzokee.domain.auth.dto.EmailDto;
//import homes.banzzokee.domain.auth.dto.EmailVerifyDto;
//import homes.banzzokee.domain.auth.dto.SignupDto;
//import homes.banzzokee.domain.auth.dto.EmailVerifyRequest;
//import homes.banzzokee.domain.auth.dto.SignInRequest;
//import homes.banzzokee.domain.auth.dto.SignupRequest;
//import homes.banzzokee.domain.auth.dto.TokenResponse;
//import homes.banzzokee.domain.auth.service.AuthService;
//import homes.banzzokee.global.security.config.SecurityConfig;
//import homes.banzzokee.global.security.jwt.JwtTokenProvider;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import org.springframework.test.web.servlet.ResultMatcher;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.times;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import static org.mockito.Mockito.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//
//import static org.mockito.Mockito.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(AuthController.class)
//@AutoConfigureMockMvc(addFilters = false)
//class AuthControllerTest {
//
//  @Autowired
//  private WebApplicationContext context;
//
//  @Autowired
//  private MockMvc mockMvc;
//
//  @MockBean
//  private AuthService authService;
//
//  @Autowired
//  private ObjectMapper objectMapper;
//
//  @MockBean
//  private JwtTokenProvider jwtTokenProvider;
//
//  @Captor
//  private ArgumentCaptor<EmailDto> captor;
//
//  @Test
//  @DisplayName("이메일 인증 코드 발송 테스트")
//  void successSendVerificationCode() throws Exception {
//    // given
//    String email = "test@test.com";
//    EmailDto emailDto = EmailDto.builder()
//        .email(email)
//        .build();
//    String requestBody = objectMapper.writeValueAsString(emailDto);
//
//    // when
//    mockMvc.perform(post("/api/auth/send-verify")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(requestBody))
//        .andExpect(status().isOk());
//    verify(authService).sendVerificationCode(captor.capture());
//    EmailDto captureDto = captor.getValue();
//
//    // then
//    verify(authService, times(1)).sendVerificationCode(captor.capture());
//    assertEquals("test@test.com", captureDto.getEmail());
//  }
//  private ArgumentCaptor<EmailVerifyRequest> captor;
//
////  @BeforeEach
////  public void setup() {
////    mockMvc = MockMvcBuilders
////        .webAppContextSetup(context)
////        .apply(springSecurity())
////package homes.banzzokee.domain.auth.controller;
////
////import static org.junit.jupiter.api.Assertions.assertEquals;
////import static org.mockito.Mockito.times;
////import static org.mockito.Mockito.verify;
////import static org.mockito.Mockito.when;
////import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
////import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
////import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
////import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
////
////import com.fasterxml.jackson.databind.ObjectMapper;
////import homes.banzzokee.domain.auth.dto.EmailDto;
////import homes.banzzokee.domain.auth.dto.EmailVerifyDto;
////import homes.banzzokee.domain.auth.dto.SignupDto;
////import homes.banzzokee.domain.auth.dto.EmailVerifyRequest;
////import homes.banzzokee.domain.auth.dto.SignInRequest;
////import homes.banzzokee.domain.auth.dto.SignupRequest;
////import homes.banzzokee.domain.auth.dto.TokenResponse;
////import homes.banzzokee.domain.auth.service.AuthService;
////import homes.banzzokee.global.security.config.SecurityConfig;
////import homes.banzzokee.global.security.jwt.JwtTokenProvider;
////import org.junit.jupiter.api.BeforeEach;
////import org.junit.jupiter.api.DisplayName;
////import org.junit.jupiter.api.Test;
////import org.junit.jupiter.api.extension.ExtendWith;
////import org.mockito.ArgumentCaptor;
////import org.mockito.Captor;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
////import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
////import org.springframework.boot.test.mock.mockito.MockBean;
////import org.springframework.context.annotation.Import;
////import org.springframework.http.MediaType;
////import org.springframework.security.test.context.support.WithMockUser;
////import org.springframework.test.context.junit.jupiter.SpringExtension;
////import org.springframework.test.web.servlet.MockMvc;
////import org.springframework.test.web.servlet.ResultMatcher;
////import org.springframework.test.web.servlet.setup.MockMvcBuilders;
////import org.springframework.web.context.WebApplicationContext;
////
////import static org.junit.jupiter.api.Assertions.*;
////import static org.mockito.Mockito.times;
////import static org.junit.jupiter.api.Assertions.assertEquals;
////import static org.mockito.BDDMockito.given;
////import static org.mockito.Mockito.verify;
////import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
////import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
////import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
////import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
////
////import static org.mockito.Mockito.*;
////import static org.mockito.ArgumentMatchers.any;
////import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
////import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
////
////import static org.mockito.Mockito.*;
////import static org.mockito.ArgumentMatchers.any;
////import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
////import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
////import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
////import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
////
////
////@ExtendWith(SpringExtension.class)
////@WebMvcTest(AuthController.class)
////@AutoConfigureMockMvc(addFilters = false)
////class AuthControllerTest {
////
////  @Autowired
////  private WebApplicationContext context;
////
////  @Autowired
////  private MockMvc mockMvc;
////
////  @MockBean
////  private AuthService authService;
////
////  @Autowired
////  private ObjectMapper objectMapper;
////
////  @MockBean
////  private AuthService authService;
////
////  @MockBean
////  private JwtTokenProvider jwtTokenProvider;
////
////  @Captor
////  private ArgumentCaptor<String> captor;
////
////  @Test
////  @DisplayName("이메일 인증 코드 발송 테스트")
////  void successSendVerificationCode() throws Exception {
////    // given
////    String email = "test@test.com";
////    EmailDto emailDto = EmailDto.builder()
////        .email(email)
////        .build();
////    String requestBody = objectMapper.writeValueAsString(emailDto);
////
////    // when
////    mockMvc.perform(post("/api/auth/send-verify")
////            .contentType(MediaType.APPLICATION_JSON)
////            .content(requestBody))
////        .andExpect(status().isOk());
////    verify(authService).sendVerificationCode(captor.capture());
////    EmailDto captureDto = captor.getValue();
////
////    // then
////    verify(authService, times(1)).sendVerificationCode(captor.capture());
////    assertEquals("test@test.com", captureDto.getEmail());
////  }
////  private ArgumentCaptor<EmailVerifyRequest> captor;
////
//////  @BeforeEach
//////  public void setup() {
//////    mockMvc = MockMvcBuilders
//////        .webAppContextSetup(context)
//////        .apply(springSecurity())
//////        .build();
//////  }
////
////  @Test
////  @DisplayName("[이메일 인증] - 성공 검증")
////  void verifyEmail_when_validInput_then_success() throws Exception {
////    // given
////    String email = "test@test.com";
////    String code = "123456";
////    EmailVerifyRequest emailVerifyRequest = EmailVerifyRequest.builder()
////        .email(email)
////        .code(code)
////        .build();
////    String requestBody = objectMapper.writeValueAsString(emailVerifyRequest);
////
////    // when & then
////    mockMvc.perform(post("/api/auth/send-verify")
////            .contentType(MediaType.APPLICATION_JSON)
////            .content(requestBody))
////        .andExpect(status().isOk());
////
////    ArgumentCaptor<EmailDto> captor = ArgumentCaptor.forClass(EmailDto.class);
////    verify(authService).sendVerificationCode(captor.capture());
////    EmailDto capturedDto = captor.getValue();
////
////    assertEquals("test@test.com", capturedDto.getEmail());
////  }
////
////  @Test
////  @WithMockUser
////  @DisplayName("[닉네임 중복 확인] - 성공 검증")
////  void checkNickname_when_validInput_then_success() throws Exception {
////    // given
////    String nickname = "반쪽이";
////    when(authService.checkNickname(nickname)).thenReturn(true);
////
////    // when & then
////    mockMvc.perform(get("/api/auth/nickname-check")
////            .param("nickname", nickname))
////        .andExpect(status().isOk())
////        .andExpect(content().string("true"));
////
////    verify(authService, times(1)).checkNickname("반쪽이");
////  }
////
////  @Test
////  @WithMockUser
////  @DisplayName("[닉네임 중복 확인] - 실패 검증")
////  void checkNickname_when_validInput_then_fail() throws Exception {
////    //given
////    String nickname = "반쪽이";
////    when(authService.checkNickname(nickname)).thenReturn(false);
////
////    //when & then
////    mockMvc.perform(get("/api/auth/nickname-check")
////            .param("nickname", nickname))
////        .andExpect(status().isOk())
////        .andExpect(content().string("false"));
////
////    verify(authService, times(1)).checkNickname("반쪽이");
////  }
////
////  @Test
////  @DisplayName("[자사 회원가입] - 성공 검증")
////  void signup_when_validInput_then_success() throws Exception {
////    // given
////    SignupRequest signupRequest = SignupRequest.builder()
////        .email("test@gmail.com")
////        .password("Password123!")
////        .confirmPassword("Password123!")
////        .nickname("test")
////        .build();
////    doNothing().when(authService).signup(any(SignupRequest.class));
////
////    // when & then
////    mockMvc.perform(post("/api/auth/sign-up")
////            .contentType(MediaType.APPLICATION_JSON)
////            .content(objectMapper.writeValueAsString(signupRequest)))
////        .andExpect(status().isOk());
////
////    verify(authService, times(1)).signup(any(SignupRequest.class));
////  }
////
////  @Test
////  @WithMockUser
////  @DisplayName("[자사 로그인] - 성공 검증")
////  void signIn_when_validInput_then_success() throws Exception {
////    SignInRequest signInRequest = SignInRequest.builder()
////        .email("test@test.com")
////        .password("@@Banzzokee12345")
////        .build();
////    TokenResponse tokenResponse = TokenResponse.builder()
////        .accessToken("Bearer !@#$%^&*()_+1234567890")
////        .refreshToken("Bearer 1234567890!@#$%^&*()")
////        .build();
////
////    given(authService.signIn(signInRequest)).willReturn(tokenResponse);
////
////    mockMvc.perform(post("/api/auth/sign-in")
////            .contentType(MediaType.APPLICATION_JSON)
////            .content(new ObjectMapper().writeValueAsString(signInRequest)))
////        .andExpect(status().isOk())
////        .andExpect((ResultMatcher) jsonPath("$.accessToken").value("Bearer !@#$%^&*()_+1234567890"))
////        .andExpect((ResultMatcher) jsonPath("$.refreshToken").value("Bearer 1234567890!@#$%^&*()"));
////  }
////}
