package homes.banzzokee.domain.review.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchQueryRepository;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.elasticsearch.document.subclass.ReviewDocumentVo;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionDocumentNotFoundException;
import homes.banzzokee.domain.adoption.exception.AdoptionIsDeletedException;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.review.dao.ReviewRepository;
import homes.banzzokee.domain.review.dto.ReviewRegisterRequest;
import homes.banzzokee.domain.review.dto.ReviewResponse;
import homes.banzzokee.domain.review.dto.ReviewSearchResponse;
import homes.banzzokee.domain.review.dto.ReviewUpdateRequest;
import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.review.exception.DeletedReviewException;
import homes.banzzokee.domain.review.exception.OneReviewPerAdoptionException;
import homes.banzzokee.domain.review.exception.ReviewNotFoundException;
import homes.banzzokee.domain.review.exception.ReviewPermissionException;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

  private final UserRepository userRepository;
  private final AdoptionRepository adoptionRepository;
  private final FileUploadService fileUploadService;
  private final ReviewRepository reviewRepository;
  private final AdoptionSearchRepository adoptionSearchRepository;
  private final AdoptionSearchQueryRepository adoptionSearchQueryRepository;

  @Transactional
  public void registerReview(ReviewRegisterRequest request, List<MultipartFile> images,
      long userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    Adoption adoption = adoptionRepository.findById(request.getAdoptionId())
        .orElseThrow(AdoptionNotFoundException::new);

    if (adoption.isDeleted()) {
      throw new AdoptionIsDeletedException();
    }

    if (!adoption.getAssignedUser().equals(user)) {
      throw new ReviewPermissionException();
    }

    if (adoption.getReview() != null && !adoption.getReview().isDeleted()) {
      throw new OneReviewPerAdoptionException();
    }

    List<S3Object> uploadedReviewImages = uploadReviewImages(images);

    Review savedReview = registerOrRestoreReviewToDataBase(request, adoption, user,
        uploadedReviewImages);

    registerReviewInAdoptionDocument(savedReview, request.getAdoptionId());
  }

  public ReviewResponse getReview(long reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(
        ReviewNotFoundException::new);
    if (review.isDeleted()) {
      throw new DeletedReviewException();
    }

    return ReviewResponse.fromEntity(review);
  }

  @Transactional
  public ReviewResponse updateReview(long reviewId, ReviewUpdateRequest request,
      List<MultipartFile> images, long userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(ReviewNotFoundException::new);
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    if (review.isDeleted()) {
      throw new DeletedReviewException();
    }

    if (!review.getUser().equals(user)) {
      throw new NoAuthorizedException();
    }

    List<S3Object> oldImages = review.getImages();
    List<S3Object> uploadedReviewImages = uploadReviewImages(images);

    review.update(request.getTitle(), request.getContent(), uploadedReviewImages);
    Review savedReview = reviewRepository.save(review);

    registerReviewInAdoptionDocument(savedReview, savedReview.getAdoption().getId());

    deleteOldImages(oldImages);
    return ReviewResponse.fromEntity(savedReview);
  }

  @Transactional
  public void deleteReview(long reviewId, long userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(ReviewNotFoundException::new);
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    if (review.isDeleted()) {
      throw new DeletedReviewException();
    }

    if (!review.getUser().equals(user)) {
      throw new NoAuthorizedException();
    }

    deleteReviewInAdoptionDocument(review.getAdoption().getId());

    LocalDateTime now = LocalDateTime.now();
    review.delete(now);
    reviewRepository.save(review);
  }

  public Slice<ReviewSearchResponse> getReviewList(Pageable pageable) {
    List<AdoptionDocument> allReview = adoptionSearchQueryRepository.findAllReview(
        pageable);
    return new SliceImpl<>(allReview.stream()
        .map(ReviewSearchResponse::fromDocument)
        .toList());
  }

  private void deleteReviewInAdoptionDocument(long adoptionId) {
    AdoptionDocument adoptionDocument = adoptionSearchRepository.findById(adoptionId)
        .orElseThrow(AdoptionDocumentNotFoundException::new);
    adoptionDocument.deleteReview();
    adoptionSearchRepository.save(adoptionDocument);
  }

  private void registerReviewInAdoptionDocument(Review savedReview,
      long adoptionId) {
    AdoptionDocument adoptionDocument = adoptionSearchRepository.findById(adoptionId)
        .orElseThrow(AdoptionDocumentNotFoundException::new);

    adoptionDocument.updateReview(ReviewDocumentVo.fromEntity(savedReview));

    adoptionSearchRepository.save(adoptionDocument);
  }


  private List<S3Object> uploadReviewImages(List<MultipartFile> images) {
    if (images == null) {
      return null;
    }
    return fileUploadService.uploadManyFile(images, FilePath.REVIEW).stream()
        .map(fileDto -> new S3Object(fileDto.getUrl()))
        .collect(Collectors.toList());
  }

  private Review registerOrRestoreReviewToDataBase(ReviewRegisterRequest request,
      Adoption adoption,
      User user, List<S3Object> images) {
    Review review = adoption.getReview();
    if (review == null) {
      review = Review.builder()
          .adoption(adoption)
          .user(user)
          .title(request.getTitle())
          .content(request.getContent())
          .images(images)
          .build();
      adoption.updateReview(review);
      adoptionRepository.save(adoption);
    }

    if (review.isDeleted()) {
      review.restore();
    }

    return reviewRepository.save(review);
  }

  private void deleteOldImages(List<S3Object> oldImages) {
    if (oldImages == null) {
      return;
    }
    for (S3Object image : oldImages) {
      if (image == null) {
        continue;
      }
      try {
        fileUploadService.deleteFile(image.getFileName());
      } catch (Exception e) {
        log.error("delete review image failed. file={}", image, e);
      }
    }
  }

}
