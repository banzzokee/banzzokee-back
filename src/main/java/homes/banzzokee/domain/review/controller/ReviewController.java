package homes.banzzokee.domain.review.controller;

import homes.banzzokee.domain.review.dto.ReviewListResponse;
import homes.banzzokee.domain.review.dto.ReviewRegisterRequest;
import homes.banzzokee.domain.review.dto.ReviewResponse;
import homes.banzzokee.domain.review.dto.ReviewUpdateRequest;
import homes.banzzokee.domain.review.service.ReviewService;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.validator.annotation.FileDuplicateValid;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public void registerReview(@Valid @RequestPart ReviewRegisterRequest request,
      @FileDuplicateValid @ImageFile @Size(min = 1, max = 8) List<MultipartFile> images,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    reviewService.registerReview(request, images, userDetails.getUserId());
  }

  @GetMapping("/{reviewId}")
  public ReviewResponse getReview(@PathVariable long reviewId) {
    return reviewService.getReview(reviewId);
  }

  @PutMapping("/{reviewId}")
  public ReviewResponse updateReview(@PathVariable long reviewId,
      @Valid @RequestPart ReviewUpdateRequest request,
      @FileDuplicateValid @ImageFile @Size(min = 1, max = 8) List<MultipartFile> images,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return reviewService.updateReview(reviewId, request, images, userDetails.getUserId());
  }

  @DeleteMapping("/{reviewId}")
  public void deleteReview(@PathVariable long reviewId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    reviewService.deleteReview(reviewId, userDetails.getUserId());
  }

  @GetMapping
  public Slice<ReviewListResponse> getReviewList(@RequestParam int page,
      @RequestParam int size, @RequestParam String direction) {
    PageRequest pageRequest = PageRequest.of(page, size,
        Sort.by(Direction.fromString(direction), "createdAt"));
    return reviewService.getReviewList(pageRequest);
  }
}
