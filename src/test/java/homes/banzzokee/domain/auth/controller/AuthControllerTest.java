package homes.banzzokee.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import homes.banzzokee.domain.auth.dto.EmailDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

  @Captor
  private ArgumentCaptor<EmailDto> captor;

  @Test
  @DisplayName("이메일 인증 코드 발송 테스트")
  void successSendVerificationCode() throws Exception {
    // given
    String email = "test@test.com" ;
    EmailDto emailDto = EmailDto.builder()
        .email(email)
        .build();
    String requestBody = objectMapper.writeValueAsString(emailDto);

    // when
    mockMvc.perform(post("/api/auth/send-verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk());
    verify(authService).sendVerificationCode(captor.capture());
    EmailDto captureDto = captor.getValue();

    // then
    verify(authService, times(1)).sendVerificationCode(captor.capture());
    assertEquals("test@test.com", captureDto.getEmail());
  }
}