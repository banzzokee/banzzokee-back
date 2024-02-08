package homes.banzzokee.consumer.error.exception;

public class TokenNotFoundException extends BusinessException {

  public TokenNotFoundException(String token) {
    super("fcm token not found, token=" + token);
  }
}
