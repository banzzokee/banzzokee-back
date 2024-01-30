package homes.banzzokee.domain.notification.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.notification.dto.FcmTokenRegisterRequest;
import homes.banzzokee.domain.notification.service.NotificationService;
import homes.banzzokee.global.util.MockMvcUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = NotificationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private NotificationService notificationService;

  @Test
  @DisplayName("[토큰 등록] - 성공 검증")
  void registerFcmToken_when_validInput_then_success() throws Exception {
    // given
    FcmTokenRegisterRequest request = FcmTokenRegisterRequest.builder()
        .token("token")
        .build();

    // when
    ResultActions resultActions = MockMvcUtil.performPut(mockMvc,
        "/api/notifications/tokens?userId=1", request);

    // then
    resultActions.andExpect(status().isOk());
    verify(notificationService).registerFcmToken(eq(request),
        eq("spring boot unit test"),
        eq(1L));
  }
}