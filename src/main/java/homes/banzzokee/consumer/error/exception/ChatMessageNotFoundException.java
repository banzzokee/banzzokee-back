package homes.banzzokee.consumer.error.exception;

public class ChatMessageNotFoundException extends BusinessException {

  public ChatMessageNotFoundException(Long id) {
    super("chat message not found, id=" + id);
  }
}
