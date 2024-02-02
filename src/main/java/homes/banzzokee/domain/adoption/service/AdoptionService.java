package homes.banzzokee.domain.adoption.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.dto.AdoptionRegisterRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionResponse;
import homes.banzzokee.domain.adoption.dto.AdoptionUpdateRequest;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionIsDeletedException;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.shelter.exception.NotVerifiedShelterExistsException;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  public AdoptionResponse getAdoption(long adoptionId) {
    Adoption adoption = findByAdoptionIdOrThrow(adoptionId);
    throwIfAdoptionIsDeleted(adoption);

    return AdoptionResponse.fromEntity(adoption);
  }

  @Transactional
  public void updateAdoption(long adoptionId, AdoptionUpdateRequest request,
      List<MultipartFile> images, long userId) {
    Adoption adoption = findByAdoptionIdOrThrow(adoptionId);
    throwIfRequestUserIsNotMatchedAdoptionWriter(adoption, userId);

    List<S3Object> oldImages = adoption.getImages();
    List<S3Object> newImages = uploadAdoptionImages(images);

    adoption.updateAdoption(
        request.getTitle(),
        request.getContent(),
        BreedType.findByString(request.getBreed()),
        DogSize.findByString(request.getSize()),
        request.isNeutering(),
        DogGender.findByString(request.getGender()),
        request.getAge(),
        request.isHealthChecked(),
        LocalDate.parse(request.getRegisteredAt()),
        newImages);

    adoptionRepository.save(adoption);
    deleteOldImages(oldImages);
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
        .breed(BreedType.findByString(request.getBreed()))
        .size(DogSize.findByString(request.getSize()))
        .neutering(request.isNeutering())
        .gender(DogGender.findByString(request.getGender()))
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
