package homes.banzzokee.global.config.stomp.principal;

import java.security.Principal;

/**
 * STOMP 접근 유저 name
 */
public class StompPrincipal implements Principal {

  private String name;

  public StompPrincipal(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
