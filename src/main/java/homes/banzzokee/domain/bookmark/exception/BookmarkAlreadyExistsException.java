package homes.banzzokee.domain.bookmark.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.BOOKMARK_ALREADY_EXISTS;

public class BookmarkAlreadyExistsException extends CustomException {

  public BookmarkAlreadyExistsException() {
    super(BOOKMARK_ALREADY_EXISTS);
  }
}
