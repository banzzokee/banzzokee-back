package homes.banzzokee.global.security;

import homes.banzzokee.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

  private final Long userId;
  private final String username;
  private final Set<GrantedAuthority> authorities;

  public UserDetailsImpl(User user, Set<GrantedAuthority> authorities) {
    this.userId = user.getId();
    this.username = user.getEmail();
    this.authorities = authorities;
  }

  /**
   * 사용자 권한 반환
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  /**
   * 사용자 비밀번호 반환
   */
  @Override
  public String getPassword() {
    return null;
  }

  /**
   * 사용자 이메일 반환
   */
  @Override
  public String getUsername() {
    return username;
  }

  /**
   * 사용자 계정이 만료되지 않았음을 확인
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * 사용자 계정이 잠기지 않았음을 확인
   */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * 사용자의 자격 증명이 만료되지 안핬음 확인
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * 사용자 계정 활성 상태 확인
   */
  @Override
  public boolean isEnabled() {
    return true;
  }
}
