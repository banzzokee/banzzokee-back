package homes.banzzokee.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import homes.banzzokee.domain.auth.dto.EmailVerifyDto;
import homes.banzzokee.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;

  @Captor
  private ArgumentCaptor<EmailVerifyDto> captor;

  @Test
  @DisplayName("이메일 인증 성공 테스트")
  void successVerifyEmail() throws Exception {
    // given
    String email = "test@test.com";
    String code = "123456";
    EmailVerifyDto emailVerifyDto = EmailVerifyDto.builder()
        .email(email)
        .code(code)
        .build();
    String requestBody = objectMapper.writeValueAsString(emailVerifyDto);

    // when & then
    mockMvc.perform(post("/api/auth/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk());
    verify(authService).verifyEmail(captor.capture());
    EmailVerifyDto capturedDto = captor.getValue();

    assertEquals("test@test.com", capturedDto.getEmail());
    assertEquals("123456", capturedDto.getCode());
  }

  @Test
  @WithMockUser
  @DisplayName("닉네임 중복확인 테스트 - 성공 케이스")
  void successCheckNickname() throws Exception {
    // given
    String nickname = "반쪽이";
    when(authService.checkNickname(nickname)).thenReturn(true);

    // when & then
    mockMvc.perform(get("/api/auth/nickname-check")
            .param("nickname", nickname))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));

    verify(authService, times(1)).checkNickname("반쪽이");
  }

  @Test
  @WithMockUser
  @DisplayName("닉네임 중복확인 테스트 - 실패 케이스")
  void failCheckNickname() throws Exception {
    //given
    String nickname = "반쪽이";
    when(authService.checkNickname(nickname)).thenReturn(false);

    //when & then
    mockMvc.perform(get("/api/auth/nickname-check")
            .param("nickname", nickname))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));

    verify(authService, times(1)).checkNickname("반쪽이");
  }
}