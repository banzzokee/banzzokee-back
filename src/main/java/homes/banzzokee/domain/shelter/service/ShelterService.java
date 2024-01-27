package homes.banzzokee.domain.shelter.service;

import homes.banzzokee.domain.shelter.dto.ShelterRegisterRequest;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.shelter.exception.NotVerifiedShelterExistsException;
import homes.banzzokee.domain.shelter.exception.UserAlreadyRegisterShelterException;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ShelterService {

  private final UserRepository userRepository;
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
        .build());
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

  private User findByUserIdOrThrow(long userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }

  private S3Object uploadShelterImage(MultipartFile shelterImage) {
    return S3Object.from(s3Service.uploadOneFile(shelterImage));
  }
}

