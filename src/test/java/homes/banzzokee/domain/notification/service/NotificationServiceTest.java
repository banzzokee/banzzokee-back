package homes.banzzokee.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.notification.dao.FcmTokenRepository;
import homes.banzzokee.domain.notification.dto.FcmTokenRegisterRequest;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.event.FcmTokenRegisteredEvent;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @InjectMocks
  private NotificationService notificationService;

  @Mock
  private FcmTokenRepository fcmTokenRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  private final static String USER_AGENT = "userAgent";

  private final static FcmTokenRegisterRequest TOKEN_REGISTER_REQUEST
      = FcmTokenRegisterRequest.builder()
      .token("token")
      .build();

  @Test
  @DisplayName("[토큰 등록] - 사용자를 못찾으면 UserNotFoundException 발생")
  void registerFcmToken_when_userNotExists_then_throwUserNotFoundException() {
    // given
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(UserNotFoundException.class,
        () -> notificationService.registerFcmToken(TOKEN_REGISTER_REQUEST,
            USER_AGENT,
            1L));
  }

  @Test
  @DisplayName("[토큰 등록] - 사용자가 토큰을 등록한 사용자가 아니면 NoAuthorizedException 발생")
  void registerFcmToken_when_userIsNotTokenUser_then_throwNoAuthorizedException() {
    // given
    User user1 = mock(User.class);
    given(user1.getId()).willReturn(1L);
    given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));

    User user2 = mock(User.class);
    given(user2.getId()).willReturn(2L);
    FcmToken token = mock(FcmToken.class);
    given(token.getUser()).willReturn(user2);

    given(fcmTokenRepository.findByToken(anyString())).willReturn(Optional.of(token));

    // when & then
    assertThrows(NoAuthorizedException.class,
        () -> notificationService.registerFcmToken(TOKEN_REGISTER_REQUEST,
            USER_AGENT,
            1L));
  }

  @Test
  @DisplayName("[토큰 등록] - 토큰이 존재하면 refresh() 호출 검증")
  void registerFcmToken_when_tokenExists_then_refreshToken() {
    // given
    LocalDateTime now = LocalDateTime.now();

    User user = mock(User.class);
    given(user.getId()).willReturn(1L);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    FcmToken token = spy(FcmToken.builder()
        .token(TOKEN_REGISTER_REQUEST.getToken())
        .userAgent(USER_AGENT)
        .user(user)
        .build());
    given(fcmTokenRepository.findByToken(token.getToken())).willReturn(
        Optional.of(token));

    // when
    notificationService.registerFcmToken(TOKEN_REGISTER_REQUEST,
        USER_AGENT,
        1L);

    // then
    verify(token).refresh();
    assertTrue(token.getLastUsedAt().isAfter(now));
  }

  @Test
  @DisplayName("[토큰 등록] - 토큰이 없으면 새로 추가")
  void registerFcmToken_when_tokenNotExists_then_saveToken() {
    // given
    LocalDateTime now = LocalDateTime.now();

    User user = mock(User.class);
    given(user.getId()).willReturn(1L);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(fcmTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

    FcmToken token = spy(FcmToken.builder()
        .token(TOKEN_REGISTER_REQUEST.getToken())
        .userAgent(USER_AGENT)
        .user(user)
        .build());
    given(fcmTokenRepository.save(any(FcmToken.class))).willReturn(token);

    // when
    notificationService.registerFcmToken(TOKEN_REGISTER_REQUEST,
        USER_AGENT,
        1L);

    // then
    ArgumentCaptor<FcmToken> tokenCaptor = ArgumentCaptor.forClass(FcmToken.class);
    verify(fcmTokenRepository).save(tokenCaptor.capture());

    FcmToken value = tokenCaptor.getValue();
    assertEquals(TOKEN_REGISTER_REQUEST.getToken(), value.getToken());
    assertEquals(USER_AGENT, value.getUserAgent());
    assertEquals(user.getId(), value.getUser().getId());
    assertTrue(value.getLastUsedAt().isAfter(now));

    ArgumentCaptor<FcmTokenRegisteredEvent> eventCaptor
        = ArgumentCaptor.forClass(FcmTokenRegisteredEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    FcmTokenRegisteredEvent event = eventCaptor.getValue();
    assertEquals(TOKEN_REGISTER_REQUEST.getToken(), event.getPayload().getToken());
    assertEquals(user.getId(), event.getPayload().getUserId());
  }
}