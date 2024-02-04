package homes.banzzokee.consumer.config;

import homes.banzzokee.consumer.error.CustomErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfig {

  @Bean
  CustomErrorHandler customErrorHandler() {
    return new CustomErrorHandler();
  }
}
