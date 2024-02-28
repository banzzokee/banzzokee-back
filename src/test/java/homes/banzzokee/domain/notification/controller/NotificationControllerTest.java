package homes.banzzokee.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.notification.dto.FcmTokenRegisterRequest;
import homes.banzzokee.domain.notification.dto.NotificationDto;
import homes.banzzokee.domain.notification.service.NotificationService;
import homes.banzzokee.global.security.WithMockCustomUser;
import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import homes.banzzokee.global.util.MockMvcUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = NotificationController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = ASSIGNABLE_TYPE,
            classes = {JwtAuthenticationFilter.class})
    })
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private NotificationService notificationService;

  @Test
  @DisplayName("[토큰 등록] - 성공 검증")
  @WithMockCustomUser
  void registerFcmToken_when_validInput_then_success() throws Exception {
    // given
    FcmTokenRegisterRequest request = FcmTokenRegisterRequest.builder()
        .token("token")
        .build();

    // when
    ResultActions resultActions = MockMvcUtil.performPut(mockMvc,
        "/api/notifications/tokens", request);

    // then
    resultActions.andExpect(status().isOk());
    verify(notificationService).registerFcmToken(eq(request),
        eq("spring boot unit test"),
        eq(1L));
  }

  @Test
  @DisplayName("[알림 목록 조회] - 성공 검증")
  @WithMockCustomUser
  void getNotificationList_when_validInput_then_success() throws Exception {
    // given
    given(notificationService.getNotificationList(any(), any(), any()))
        .willReturn(new SliceImpl<>(List.of(
            NotificationDto.builder()
                .id(1L)
                .notifiedAt(LocalDateTime.now())
                .build()
        ), PageRequest.of(0, 5), false));

    // when
    ResultActions resultActions = MockMvcUtil.performGet(mockMvc,
        "/api/notifications?page=0&size=5&checked=false");

    // then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1L))
        .andExpect(jsonPath("$.content[0].notifiedAt").exists());

    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(
        PageRequest.class);
    verify(notificationService).getNotificationList(
        pageRequestCaptor.capture(),
        eq(false),
        eq(1L));
  }

  @Test
  @DisplayName("[알림 읽음] - 성공 검증")
  @WithMockCustomUser
  void checkNotification_when_validInput_then_success() throws Exception {
    // when
    ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
        "/api/notifications/1/check", null);

    // then
    resultActions.andExpect(status().isOk());
    verify(notificationService).checkNotification(1L, 1L);
  }

  @Test
  @DisplayName("[알림 모두 읽음] - 성공 검증")
  @WithMockCustomUser
  void checkAllNotifications_when_validInput_then_success() throws Exception {
    // when
    ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
        "/api/notifications/check-all", null);

    // then
    resultActions.andExpect(status().isOk());
    verify(notificationService).checkAllNotifications(1L);
  }
}