package homes.banzzokee.domain.user.service;

import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public UserProfileDto getUserProfile(long userId) {
    return UserProfileDto.fromEntity(
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new)
    );
  }
}
