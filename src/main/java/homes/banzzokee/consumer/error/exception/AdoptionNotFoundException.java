package homes.banzzokee.consumer.error.exception;

public class AdoptionNotFoundException extends BusinessException {

  public AdoptionNotFoundException(Long id) {
    super("adoption not found, id=" + id);
  }
}
