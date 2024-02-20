package homes.banzzokee.domain.adoption.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.dto.AdoptionRegisterRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionResponse;
import homes.banzzokee.domain.adoption.dto.AdoptionSearchRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionSearchResponse;
import homes.banzzokee.domain.adoption.dto.AdoptionStatusChangeRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionUpdateRequest;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchQueryRepository;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionDocumentNotFoundException;
import homes.banzzokee.domain.adoption.exception.AdoptionIsDeletedException;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.adoption.exception.AlreadyFinishedAdoptionException;
import homes.banzzokee.domain.adoption.exception.AssignedUserMustBeNullException;
import homes.banzzokee.domain.adoption.exception.CurrentStatusIsSameToChangeException;
import homes.banzzokee.domain.adoption.exception.MustInputAssignedUserInfoException;
import homes.banzzokee.domain.bookmark.dao.BookmarkRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.shelter.exception.NotVerifiedShelterExistsException;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.FollowRepository;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import java.time.LocalDate;
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
public class AdoptionService {

  private final UserRepository userRepository;
  private final FileUploadService fileUploadService;
  private final AdoptionRepository adoptionRepository;
  private final AdoptionSearchRepository adoptionSearchRepository;
  private final AdoptionSearchQueryRepository queryRepository;
  private final BookmarkRepository bookmarkRepository;
  private final FollowRepository followRepository;

  @Transactional
  public void registerAdoption(AdoptionRegisterRequest request,
      List<MultipartFile> images,
      long userId) {
    User user = findByUserIdOrThrow(userId);
    Shelter shelter = throwIfShelterIsDeletedOrNotExist(user);
    throwIfShelterIsNotVerified(shelter);

    List<S3Object> uploadedImages = uploadAdoptionImages(images);

    Adoption savedAdoption = registerAdoptionToDataBase(request, user, uploadedImages);
    registerAdoptionToElasticSearch(savedAdoption);
  }

  public AdoptionResponse getAdoption(long adoptionId, UserDetailsImpl userDetails) {
    Adoption adoption = findByAdoptionIdOrThrow(adoptionId);
    throwIfAdoptionIsDeleted(adoption);
    AdoptionResponse response = AdoptionResponse.fromEntity(adoption);
    if (userDetails != null) {
      boolean isBookmarked = bookmarkRepository.findByUserIdAndAdoptionId(
          userDetails.getUserId(), adoptionId).isPresent();

      boolean isFollowed = followRepository.findByFolloweeIdAndFollowerId(
          adoption.getUser().getId(), userDetails.getUserId()).isPresent();

      response.updateIsBookmarkedAndIsFollowed(isBookmarked, isFollowed);
    }
    return response;
  }

  @Transactional
  public void updateAdoption(long adoptionId, AdoptionUpdateRequest request,
      List<MultipartFile> images, long userId) {
    Adoption adoption = findByAdoptionIdOrThrow(adoptionId);
    throwIfAdoptionIsDeleted(adoption);
    if (adoption.getStatus().equals(AdoptionStatus.FINISHED)) {
      throw new AlreadyFinishedAdoptionException();
    }
    throwIfRequestUserIsNotMatchedAdoptionWriter(adoption, userId);
    Shelter shelter = throwIfShelterIsDeletedOrNotExist(adoption.getUser());
    throwIfShelterIsNotVerified(shelter);

    List<S3Object> oldImages = adoption.getImages();
    List<S3Object> newImages = uploadAdoptionImages(images);

    adoption.updateAdoption(
        request.getTitle(),
        request.getContent(),
        request.getBreed(),
        request.getSize(),
        request.isNeutering(),
        request.getGender(),
        request.getAge(),
        request.isHealthChecked(),
        LocalDate.parse(request.getRegisteredAt()),
        newImages);

    Adoption savedAdoption = adoptionRepository.save(adoption);

    AdoptionDocument adoptionDocument = adoptionSearchRepository.findById(
        savedAdoption.getId()).orElseThrow(AdoptionDocumentNotFoundException::new);
    adoptionDocument.update(savedAdoption);
    adoptionSearchRepository.save(adoptionDocument);

    deleteOldImages(oldImages);
  }

  @Transactional
  public void changeAdoptionStatus(long adoptionId, AdoptionStatusChangeRequest request,
      long userId) {
    if (request.getStatus().equals(AdoptionStatus.FINISHED)
        && request.getAssignedUserId() == null) {
      throw new MustInputAssignedUserInfoException();
    }

    if (request.getStatus().equals(AdoptionStatus.RESERVING)
        || request.getStatus().equals(AdoptionStatus.ADOPTING)) {
      if (request.getAssignedUserId() != null) {
        throw new AssignedUserMustBeNullException();
      }
    }

    Adoption adoption = findByAdoptionIdOrThrow(adoptionId);
    throwIfAdoptionIsDeleted(adoption);
    throwIfRequestUserIsNotMatchedAdoptionWriter(adoption, userId);
    Shelter shelter = throwIfShelterIsDeletedOrNotExist(adoption.getUser());
    throwIfShelterIsNotVerified(shelter);

    // 변경하려는 분양게시글 상태가 현재 상태와 같으면 예외 발생
    if (adoption.getStatus().equals(request.getStatus())) {
      throw new CurrentStatusIsSameToChangeException();
    }

    User assignedUser = request.getAssignedUserId() == null ? null
        : findByUserIdOrThrow(request.getAssignedUserId());

    // 분양완료로 변경하려는 경우는 상태변경, 입양자 정보 입력, 입양일시 입력
    if (request.getStatus().equals(AdoptionStatus.FINISHED)) {
      adoption.updateStatusToFinish(request.getStatus(), assignedUser);
    } else {  // 분양중, 예약중으로 변경하려는 경우 상태만 변경
      adoption.updateStatusExceptToFinish(request.getStatus());
    }

    Adoption savedAdoption = adoptionRepository.save(adoption);

    AdoptionDocument adoptionDocument = adoptionSearchRepository.findById(
        savedAdoption.getId()).orElseThrow(AdoptionDocumentNotFoundException::new);
    adoptionDocument.updateStatus(savedAdoption);
    adoptionSearchRepository.save(adoptionDocument);
  }

  @Transactional
  public void deleteAdoption(long adoptionId, long userId) {
    Adoption adoption = findByAdoptionIdOrThrow(adoptionId);
    throwIfAdoptionIsDeleted(adoption);
    if (adoption.getStatus().equals(AdoptionStatus.FINISHED)) {
      throw new AlreadyFinishedAdoptionException();
    }
    throwIfRequestUserIsNotMatchedAdoptionWriter(adoption, userId);

    AdoptionDocument adoptionDocument = adoptionSearchRepository.findById(
        adoption.getId()).orElseThrow(AdoptionDocumentNotFoundException::new);

    adoption.delete();
    adoptionDocument.delete(adoption);

    adoptionRepository.save(adoption);
    adoptionSearchRepository.save(adoptionDocument);
  }

  public Slice<AdoptionSearchResponse> getAdoptionList(AdoptionSearchRequest request,
      Pageable pageable) {
    List<AdoptionSearchResponse> responses = queryRepository.findByAdoptionSearchRequest(
            request, pageable).stream()
        .map(AdoptionSearchResponse::fromDocument)
        .toList();
    return new SliceImpl<>(responses);
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
        log.error("delete adoption image failed. file={}", image, e);
      }
    }
  }

  private void throwIfRequestUserIsNotMatchedAdoptionWriter(Adoption adoption,
      long userId) {
    if (adoption.getUser().getId() != userId) {
      throw new NoAuthorizedException();
    }
  }

  private void throwIfAdoptionIsDeleted(Adoption adoption) {
    if (adoption.getDeletedAt() != null) {
      throw new AdoptionIsDeletedException();
    }
  }

  private Adoption findByAdoptionIdOrThrow(long adoptionId) {
    return adoptionRepository.findById(adoptionId)
        .orElseThrow(AdoptionNotFoundException::new);
  }

  private User findByUserIdOrThrow(long userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }

  private Shelter throwIfShelterIsDeletedOrNotExist(User user) {
    if (user.getShelter() == null || user.getShelter().isDeleted()) {
      throw new NoAuthorizedException();
    }
    return user.getShelter();
  }

  private void throwIfShelterIsNotVerified(Shelter shelter) {
    if (!shelter.isVerified()) {
      throw new NotVerifiedShelterExistsException();
    }
  }

  private List<S3Object> uploadAdoptionImages(List<MultipartFile> images) {
    if (images == null) {
      return null;
    }
    return fileUploadService.uploadManyFile(images, FilePath.ADOPTION).stream()
        .map(fileDto -> new S3Object(fileDto.getUrl()))
        .collect(Collectors.toList());
  }

  private Adoption registerAdoptionToDataBase(AdoptionRegisterRequest request, User user,
      List<S3Object> images) {
    return adoptionRepository.save(Adoption.builder()
        .user(user)
        .title(request.getTitle())
        .content(request.getContent())
        .breed(request.getBreed())
        .size(request.getSize())
        .neutering(request.isNeutering())
        .gender(request.getGender())
        .age(request.getAge())
        .healthChecked(request.isHealthChecked())
        .registeredAt(LocalDate.parse(request.getRegisteredAt()))
        .images(images)
        .status(AdoptionStatus.ADOPTING)
        .build());
  }

  private void registerAdoptionToElasticSearch(Adoption adoption) {
    adoptionSearchRepository.save(AdoptionDocument.fromEntity(adoption));
  }

}
