package homes.banzzokee.global.security.config;

import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorizeRequests -> authorizeRequests
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("(/api/users/**").hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/api/shelters/**").hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/api/adoptions/**").hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/api/reviews/**").hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/api/bookmarks/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/api/notifications/**").hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/api/rooms/**", "/api/chats/**").hasAnyRole("USER")
            .requestMatchers("/ws-stomp/**").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
  }
}
