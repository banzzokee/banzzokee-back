package homes.banzzokee.domain.bookmark.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.BOOKMARK_NOT_FOUND;

public class BookmarkNotFoundException extends CustomException {

  public BookmarkNotFoundException() {
    super(BOOKMARK_NOT_FOUND);
  }
}
