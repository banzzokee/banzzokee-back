package homes.banzzokee.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.notification.dao.FcmTokenRepository;
import homes.banzzokee.domain.notification.dto.FcmTokenRegisterRequest;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @InjectMocks
  private NotificationService notificationService;

  @Mock
  private FcmTokenRepository fcmTokenRepository;

  @Mock
  private UserRepository userRepository;

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
            "userAgent",
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
        .userAgent("userAgent")
        .user(user)
        .build());
    given(fcmTokenRepository.findByToken(token.getToken())).willReturn(
        Optional.of(token));

    // when
    notificationService.registerFcmToken(TOKEN_REGISTER_REQUEST,
        "userAgent",
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

    // when
    notificationService.registerFcmToken(TOKEN_REGISTER_REQUEST,
        "userAgent",
        1L);

    // then
    ArgumentCaptor<FcmToken> tokenCaptor = ArgumentCaptor.forClass(FcmToken.class);
    verify(fcmTokenRepository).save(tokenCaptor.capture());

    FcmToken value = tokenCaptor.getValue();
    assertEquals(TOKEN_REGISTER_REQUEST.getToken(), value.getToken());
    assertEquals("userAgent", value.getUserAgent());
    assertEquals(user.getId(), value.getUser().getId());
    assertTrue(value.getLastUsedAt().isAfter(now));
  }
}