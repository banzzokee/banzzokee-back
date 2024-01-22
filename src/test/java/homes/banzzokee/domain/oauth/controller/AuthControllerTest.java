package homes.banzzokee.domain.oauth.controller;

import homes.banzzokee.domain.oauth.dto.TokenDto;
import homes.banzzokee.domain.oauth.exception.TokenRequiredException;
import homes.banzzokee.domain.oauth.service.AuthService;
import homes.banzzokee.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AuthService authService;

  @Test
  @DisplayName("구글 로그인 토큰 발급 성공")
  @WithMockUser(username = "banzzokee", roles = "GOOGLE")
  void successGetGoogleToken() throws Exception {
    //given
    String code = "test_code";
    TokenDto expectedTokenDto = TokenDto.builder()
        .accessToken("testAccessToken")
        .refreshToken("testRefreshToken")
        .build();
    given(authService.getGoogleToken(any())).willReturn(expectedTokenDto);

    //when
    ResultActions actions = mockMvc.perform(get("/api/auth/google/callback")
        .param("code", code));

    //then
    actions.andExpect(status().isOk());
    actions.andExpect(jsonPath("$.access_token").value("testAccessToken"));
    actions.andExpect(jsonPath("$.refresh_token").value("testRefreshToken"));
  }

  @Test
  @DisplayName("구글 로그인 토큰 발급 실패")
  @WithMockUser(username = "banzzokee", roles = "GOOGLE")
  void failGetGoogleToken() throws Exception {
    //given
    String code = "test_code";
    given(authService.getGoogleToken(any()))
        .willThrow(new TokenRequiredException(ErrorCode.TOKEN_REQUIRED));

    //when
    ResultActions actions = mockMvc.perform(get("/api/auth/google/callback")
        .param("code", code));

    //then
    actions.andExpect(status().is4xxClientError());
  }
}