package homes.banzzokee.domain.bookmark.controller;

import homes.banzzokee.domain.adoption.dto.AdoptionDto;
import homes.banzzokee.domain.bookmark.dto.BookmarkRegisterRequest;
import homes.banzzokee.domain.bookmark.service.BookmarkService;
import homes.banzzokee.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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

  private static final String PAGE_DEFAULT_VALUE = "0";
  private static final String SIZE_DEFAULT_VALUE = "10";

  private final BookmarkService bookmarkService;

  @PostMapping
  public ResponseEntity<Void> registerBookmark(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @Valid @RequestBody BookmarkRegisterRequest bookmarkRegisterRequest) {
    bookmarkService.registerBookmark(userDetails, bookmarkRegisterRequest);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{adoptionId}")
        .buildAndExpand(bookmarkRegisterRequest.getAdoptionId()).toUri();
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(location).build();
  }

  @DeleteMapping("/{bookmarkId}")
  public ResponseEntity<Void> deleteBookmark(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @PathVariable long bookmarkId) {
    bookmarkService.deleteBookmark(userDetails, bookmarkId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/adoptions")
  public ResponseEntity<Slice<AdoptionDto>> findAllBookmark(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(required = false, defaultValue = PAGE_DEFAULT_VALUE) int page,
      @RequestParam(required = false, defaultValue = SIZE_DEFAULT_VALUE) int size,
      @RequestParam(required = false, defaultValue = "desc") String direction) {
    Pageable pageable = PageRequest.of(page, size,
        Sort.by(Direction.fromString(direction), "createdAt"));
    return ResponseEntity.ok(bookmarkService.findAllBookmark(userDetails, pageable));
  }
}
