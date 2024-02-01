package homes.banzzokee.domain.notification.service;

import homes.banzzokee.domain.notification.dao.FcmTokenRepository;
import homes.banzzokee.domain.notification.dto.FcmTokenDto;
import homes.banzzokee.domain.notification.dto.FcmTokenRegisterRequest;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.event.FcmTokenRegisteredEvent;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final FcmTokenRepository fcmTokenRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * FCM 토큰 등록
   *
   * @param request   토큰 등록 요청
   * @param userAgent User-Agent
   * @param userId    토큰을 등록할 사용자 아이디
   */
  @Transactional
  public void registerFcmToken(FcmTokenRegisterRequest request, String userAgent,
      long userId) {
    User user = findUserByIdOrThrow(userId);
    FcmToken fcmToken = fcmTokenRepository.findByToken(request.getToken()).orElse(null);

    if (fcmToken != null) {
      refreshFcmTokenIfAuthorized(fcmToken, user);
    } else {
      saveFcmTokenAndPublishEvent(request, user, userAgent);
    }
  }

  /**
   * 사용자를 반환한다.
   *
   * @param userId 사용자 아이디
   * @return 사용자
   */
  private User findUserByIdOrThrow(long userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }

  /**
   * 토큰을 등록한 사용자가 아니라면 예외를 발생한다.
   *
   * @param user  확인할 사용자
   * @param token 토큰
   */
  private void throwIfUserIsNotTokenUser(User user, FcmToken token) {
    if (!Objects.equals(user.getId(), token.getUser().getId())) {
      throw new NoAuthorizedException();
    }
  }

  /**
   * 권한이 있는 사용자면 토큰을 갱신한다.
   *
   * @param fcmToken FCM 토큰
   * @param user     권한 확인할 사용자
   */
  private void refreshFcmTokenIfAuthorized(FcmToken fcmToken, User user) {
    throwIfUserIsNotTokenUser(user, fcmToken);
    fcmToken.refresh();
  }

  /**
   * FCM 토큰을 저장하고 이벤트를 발행한다.
   *
   * @param request   토큰 등록 요청
   * @param user      사용자
   * @param userAgent User-Agent
   */
  private void saveFcmTokenAndPublishEvent(FcmTokenRegisterRequest request, User user,
      String userAgent) {
    FcmToken fcmToken = fcmTokenRepository.save(FcmToken.builder()
        .token(request.getToken())
        .userAgent(userAgent)
        .user(user)
        .build());

    eventPublisher.publishEvent(
        FcmTokenRegisteredEvent.from(
            FcmTokenDto.fromEntity(fcmToken)
        ));
  }
}
