package homes.banzzokee.global.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SpringSecurity configuration class
 * you can add custom Filter chain, AccessDeniedHandler, AuthenticationEntryPoint
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

    return httpSecurity.httpBasic(AbstractHttpConfigurer::disable)

        .csrf(AbstractHttpConfigurer::disable)

        .build();
  }

}
