package homes.banzzokee.domain.auth.service;

import homes.banzzokee.domain.auth.dto.SignupDto;
import homes.banzzokee.domain.user.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;

  public void signup(SignupDto signupDto) {

  }
}
