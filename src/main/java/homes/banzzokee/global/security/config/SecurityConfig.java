package homes.banzzokee.global.security.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import homes.banzzokee.global.error.AccessDeniedHandlerImpl;
import homes.banzzokee.global.error.ExceptionHandlerFilter;
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
  private final ExceptionHandlerFilter exceptionHandlerFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorizeRequests -> authorizeRequests
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/users/**").hasAnyRole("USER")
            .requestMatchers("/api/shelters/{shelterId}/verify").hasAnyRole("ADMIN")
            .requestMatchers(POST, "/api/shelters").hasAnyRole("USER", "SHELTER")
            .requestMatchers("/api/shelters/**").hasAnyRole("SHELTER")
            .requestMatchers(GET, "/api/adoptions", "/api/adoptions/{adoptionId}")
            .permitAll()
            .requestMatchers(POST, "/api/adoptions").hasRole("SHELTER")
            .requestMatchers(PUT, "api/adoptions/{adoptionId}").hasRole("SHELTER")
            .requestMatchers(PATCH, "/api/adoptions/{adoptionId}/status")
            .hasRole("SHELTER")
            .requestMatchers(DELETE, "/api/adoptions/{adoptionID}").hasRole("USER")
            .requestMatchers("/api/reviews/**").hasAnyRole("USER")
            .requestMatchers("/api/bookmarks/**").hasAnyRole("USER")
            .requestMatchers("/api/notifications/**")
            .hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/api/rooms/**", "/api/chats/**")
            .hasAnyRole("USER", "ADMIN", "SHELTER")
            .requestMatchers("/ws-stomp/**").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(exceptionHandlerFilter, JwtAuthenticationFilter.class)
        .exceptionHandling(ex -> ex.accessDeniedHandler(new AccessDeniedHandlerImpl()));
    return httpSecurity.build();
  }
}
