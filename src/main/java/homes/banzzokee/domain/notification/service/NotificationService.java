package homes.banzzokee.domain.notification.service;

import homes.banzzokee.domain.notification.dao.FcmTokenRepository;
import homes.banzzokee.domain.notification.dto.FcmTokenRegisterRequest;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final FcmTokenRepository fcmTokenRepository;
  private final UserRepository userRepository;

  @Transactional
  public void registerFcmToken(FcmTokenRegisterRequest request, String userAgent,
      long userId) {
    User user = findUserByIdOrThrow(userId);

    fcmTokenRepository.findByToken(request.getToken())
        .ifPresentOrElse(
            FcmToken::refresh,
            () -> fcmTokenRepository.save(FcmToken.builder()
                .token(request.getToken())
                .userAgent(userAgent)
                .user(user)
                .build()));
  }

  private User findUserByIdOrThrow(long userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }
}
