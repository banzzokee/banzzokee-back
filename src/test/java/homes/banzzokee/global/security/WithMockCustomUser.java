package homes.banzzokee.global.security;

import homes.banzzokee.domain.type.Role;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static homes.banzzokee.domain.type.Role.USER;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

  long userId() default 1L;

  String username() default "user1";

  Role[] roles() default {USER};
}