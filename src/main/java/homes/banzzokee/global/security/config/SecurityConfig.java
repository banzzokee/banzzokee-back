package homes.banzzokee.global.security.config;

import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
            .requestMatchers(HttpMethod.GET, "/api/adoptions", "/api/adoptions/{adoptionId}").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/adoptions").hasRole("SHELTER")
            .requestMatchers(HttpMethod.PUT, "api/adoptions/{adoptionId}").hasRole("SHELTER")
            .requestMatchers(HttpMethod.PATCH, "/api/adoptions/{adoptionId}/status").hasRole("SHELTER")
            .requestMatchers(HttpMethod.DELETE, "/api/adoptions/{adoptionID}").hasRole("USER")
            .requestMatchers("(/api/users/**").hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/api/shelters/**").hasAnyRole("ADMIN", "SHELTER")
            .requestMatchers("/api/reviews/**").hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/api/bookmarks/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/api/notifications/**").hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/api/rooms/**", "/api/chats/**").hasAnyRole("USER", "ADMIN", "SHELTER")
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
  }
}
