package homes.banzzokee.domain.user.service;

import static homes.banzzokee.global.error.ErrorCode.PASSWORD_UNMATCHED;

import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.WithdrawUserRequest;
import homes.banzzokee.domain.user.dto.WithdrawUserResponse;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserAlreadyWithdrawnException;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.CustomException;
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

  @Transactional
  public WithdrawUserResponse withdrawUser(WithdrawUserRequest request,
      long userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    throwIfAlreadyWithdrawn(user);
    throwIfPasswordUnmatched(user, request.password());
    user.withdraw();
    return WithdrawUserResponse.fromEntity(user);
  }

  private void throwIfAlreadyWithdrawn(User user) {
    if (user.isWithdrawn()) {
      throw new UserAlreadyWithdrawnException();
    }
  }

  private void throwIfPasswordUnmatched(User user, String password)
      throws CustomException {
    // TODO: Auth 회원가입 기능 완료 후, PasswordEncoder 비교 로직 추가
    if (!user.getPassword().equals(password)) {
      // TODO: Auth에 PasswordUnmatchedException 추가
      throw new CustomException(PASSWORD_UNMATCHED);
    }
  }
}
