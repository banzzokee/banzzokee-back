package homes.banzzokee.domain.bookmark.controller;

import homes.banzzokee.domain.bookmark.dto.BookmarkRegisterRequest;
import homes.banzzokee.domain.bookmark.service.BookmarkService;
import homes.banzzokee.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

  private final BookmarkService bookmarkService;

  @PostMapping
  public ResponseEntity<Void> registerBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @Valid @RequestBody BookmarkRegisterRequest bookmarkRegisterRequest) {
    bookmarkService.registerBookmark(userDetails, bookmarkRegisterRequest);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{bookmarkId}")
        .buildAndExpand(bookmarkRegisterRequest.getAdoptionId()).toUri();
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(location).build();
  }
}
