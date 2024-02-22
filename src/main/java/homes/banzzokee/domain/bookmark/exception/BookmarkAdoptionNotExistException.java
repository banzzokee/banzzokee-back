package homes.banzzokee.domain.bookmark.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.BOOKMARKED_ADOPTION_NOT_EXIST;

public class BookmarkAdoptionNotExistException extends CustomException {

  public BookmarkAdoptionNotExistException() {
    super(BOOKMARKED_ADOPTION_NOT_EXIST);
  }
}
