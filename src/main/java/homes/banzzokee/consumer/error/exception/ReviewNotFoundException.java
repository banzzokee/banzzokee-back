package homes.banzzokee.consumer.error.exception;

public class ReviewNotFoundException extends BusinessException {

  public ReviewNotFoundException(Long id) {
    super("review not found, id=" + id);
  }
}
