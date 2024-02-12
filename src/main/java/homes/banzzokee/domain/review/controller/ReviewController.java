package homes.banzzokee.domain.review.controller;

import homes.banzzokee.domain.review.dto.ReviewRegisterRequest;
import homes.banzzokee.domain.review.service.ReviewService;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.validator.annotation.FileDuplicateValid;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
