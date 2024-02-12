package homes.banzzokee.global.security;

import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.util.MockDataUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements
    WithSecurityContextFactory<WithMockCustomUser> {

  @Override
  public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
    SecurityContext context = SecurityContextHolder.getContext();
    User user = MockDataUtil.createMockUser(annotation.userId(), annotation.username(),
        annotation.roles());
    List<GrantedAuthority> authorities = user.getRole().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        .collect(Collectors.toList());
    UserDetails userDetails = new UserDetailsImpl(user, authorities);
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
        null, userDetails.getAuthorities());
    context.setAuthentication(authentication);
    return context;
  }
}