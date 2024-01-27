package homes.banzzokee.domain.shelter.service;

import static homes.banzzokee.domain.type.Role.ADMIN;
import static homes.banzzokee.global.error.ErrorCode.FAILED;

import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.dto.ShelterRegisterRequest;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.shelter.exception.ShelterAlreadyVerifiedException;
import homes.banzzokee.domain.shelter.exception.ShelterNotFoundException;
import homes.banzzokee.domain.shelter.exception.NotVerifiedShelterExistsException;
import homes.banzzokee.domain.shelter.exception.UserAlreadyRegisterShelterException;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.CustomException;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ShelterService {

  private final UserRepository userRepository;
  private final ShelterRepository shelterRepository;
  private final FileUploadService s3Service;

  @Transactional
  public void registerShelter(ShelterRegisterRequest request, MultipartFile shelterImg,
      long userId) {
    User user = findByUserIdOrThrow(userId);

    throwIfUserAlreadyRegisterShelter(user);
    throwIfShelterNotVerified(user.getShelter());

    S3Object uploadedImage = uploadShelterImage(shelterImg);

    user.registerShelter(Shelter.builder()
        .name(request.getName())
        .description(request.getDescription())
        .tel(request.getTel())
        .address(request.getAddress())
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .shelterImgUrl(uploadedImage.getUrl())
        .verified(false)
        .user(user)
        .build());
  }

  public void verifyShelter(long shelterId, long userId) {
    User user = findByUserIdOrThrow(userId);
    throwIfUserHasNotAdminRole(user);

    Shelter shelter = findByShelterIdOrThrow(shelterId);
    throwIfShelterAlreadyVerified(shelter);

    shelter.verify();
  }

  private void throwIfShelterNotVerified(Shelter shelter) {
    if (shelter != null && !shelter.isVerified()) {
      throw new NotVerifiedShelterExistsException();
    }
  }

  private void throwIfUserAlreadyRegisterShelter(User user) {
    if (user.getShelter() != null && user.getShelter().isVerified()) {
      throw new UserAlreadyRegisterShelterException();
    }
  }

  private void throwIfUserHasNotAdminRole(User user) throws CustomException {
    // TODO: 권한 제어 설정 후 삭제 확인
    if (!user.getRole().contains(ADMIN)) {
      throw new CustomException(FAILED);
    }
  }

  private void throwIfShelterAlreadyVerified(Shelter shelter) {
    if (shelter.isVerified()) {
      throw new ShelterAlreadyVerifiedException(shelter.getId());
    }
  }

  private User findByUserIdOrThrow(long userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }

  private Shelter findByShelterIdOrThrow(long shelterId) {
    return shelterRepository.findById(shelterId)
        .orElseThrow(() -> new ShelterNotFoundException(shelterId));
  }

  private S3Object uploadShelterImage(MultipartFile shelterImage) {
    return S3Object.from(s3Service.uploadOneFile(shelterImage));
  }
}

