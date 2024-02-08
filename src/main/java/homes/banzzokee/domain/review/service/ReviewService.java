package homes.banzzokee.domain.review.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionDocumentNotFoundException;
import homes.banzzokee.domain.adoption.exception.AdoptionIsDeletedException;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.review.dao.ReviewRepository;
import homes.banzzokee.domain.review.dto.ReviewDto;
import homes.banzzokee.domain.review.dto.ReviewRegisterRequest;
import homes.banzzokee.domain.review.elasticsearch.dao.ReviewDocumentRepository;
import homes.banzzokee.domain.review.elasticsearch.document.ReviewDocument;
import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.review.exception.OneReviewPerAdoptionException;
import homes.banzzokee.domain.review.exception.ReviewPermissionException;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final UserRepository userRepository;
  private final AdoptionRepository adoptionRepository;
  private final FileUploadService fileUploadService;
  private final ReviewRepository reviewRepository;
  private final ReviewDocumentRepository reviewDocumentRepository;
  private final AdoptionSearchRepository adoptionSearchRepository;

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

    Review savedReview = registerReviewToDataBase(request, adoption, user,
        uploadedReviewImages);

    registerReviewInAdoptionDocument(savedReview, request.getAdoptionId());

    reviewDocumentRepository.save(ReviewDocument.fromEntity(savedReview));

  }

  private void registerReviewInAdoptionDocument(Review savedReview,
      long adoptionId) {
    AdoptionDocument adoptionDocument = adoptionSearchRepository.findById(adoptionId)
        .orElseThrow(AdoptionDocumentNotFoundException::new);

    adoptionDocument.updateReview(ReviewDto.fromEntity(savedReview));

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

  private Review registerReviewToDataBase(ReviewRegisterRequest request,
      Adoption adoption,
      User user, List<S3Object> images) {
    return reviewRepository.save(Review.builder()
        .adoption(adoption)
        .user(user)
        .title(request.getTitle())
        .content(request.getContent())
        .images(images)
        .build());
  }

}
