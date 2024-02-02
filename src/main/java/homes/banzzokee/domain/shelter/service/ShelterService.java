package homes.banzzokee.domain.shelter.service;

import static homes.banzzokee.domain.type.Role.ADMIN;

import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.dto.ShelterRegisterRequest;
import homes.banzzokee.domain.shelter.dto.ShelterUpdateRequest;
import homes.banzzokee.domain.shelter.dto.ShelterUpdateResponse;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.shelter.exception.NotVerifiedShelterExistsException;
import homes.banzzokee.domain.shelter.exception.ShelterAlreadyVerifiedException;
import homes.banzzokee.domain.shelter.exception.ShelterNotFoundException;
import homes.banzzokee.domain.shelter.exception.UserAlreadyRegisterShelterException;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.infra.fileupload.service.FileUploadService;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShelterService {

  private final UserRepository userRepository;
  private final ShelterRepository shelterRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final FileUploadService s3Service;

  /**
   * 보호소 등록
   *
   * @param request    보호소 등록 요청
   * @param shelterImg 보호소 이미지
   * @param userId     보호소를 등록할 사용자 아이디
   */
  @Transactional
  public void registerShelter(ShelterRegisterRequest request, MultipartFile shelterImg,
                              long userId) {
    User user = findByUserIdOrThrow(userId);

    throwIfUserAlreadyRegisterShelter(user);
    throwIfShelterNotVerified(user.getShelter());

    S3Object uploadedImage = uploadShelterImage(shelterImg);
    registerOrRestoreShelter(user, request, uploadedImage);
  }

  /**
   * 보호소 승인
   *
   * @param shelterId 승인할 보호소 아이디
   * @param userId    보호소를 승인할 사용자 아이디
   */
  @Transactional
  public void verifyShelter(long shelterId, long userId) {
    User user = findByUserIdOrThrow(userId);
    throwIfUserHasNotAdminRole(user);

    Shelter shelter = findByShelterIdOrThrow(shelterId);
    throwIfShelterAlreadyVerified(shelter);

    shelter.verify();
  }

  /**
   * 보호소 정보 수정
   *
   * @param shelterId    수정할 보호소 아이디
   * @param request      보호소 수정 요청
   * @param shelterImage 보호소 이미지
   * @param userId       보호소를 수정할 사용자 아이디
   * @return 보호소 수정 응답
   */
  @Transactional
  public ShelterUpdateResponse updateShelter(long shelterId, ShelterUpdateRequest request,
                                             MultipartFile shelterImage, long userId) {
    User user = findByUserIdOrThrow(userId);
    Shelter shelter = findByShelterIdOrThrow(shelterId);

    throwIfUserIsNotShelterUser(user, shelter);

    S3Object oldShelterImage = shelter.getShelterImage();
    S3Object uploadedImage = uploadShelterImage(shelterImage);
    shelter.updateProfile(request.getName(), request.getDescription(), request.getTel(),
        request.getAddress(), request.getLatitude(), request.getLongitude(),
        uploadedImage);
    deleteOldShelterImageIfExists(oldShelterImage);

    return ShelterUpdateResponse.fromEntity(shelter);
  }

  /**
   * 보호소 삭제
   *
   * @param shelterId 삭제할 보호소 아이디
   * @param userId    보호소를 삭제할 사용자 아이디
   */
  @Transactional
  public void unregisterShelter(long shelterId, long userId) {
    User user = findByUserIdOrThrow(userId);
    Shelter shelter = findByShelterIdOrThrow(shelterId);

    throwIfUserIsNotShelterUser(user, shelter);

    user.unregisterShelter();

    // TODO: 쿼리 최적화 (queryDSL 필요할 듯)
    chatRoomRepository
        .findAllByShelterId(shelterId)
        .forEach(ChatRoom::leaveShelter);
  }

  /**
   * 보호소를 등록한 사용자가 아니라면 예외를 발생한다.
   *
   * @param user    확인할 사용자
   * @param shelter 보호소
   */
  private void throwIfUserIsNotShelterUser(User user, Shelter shelter) {
    if (!Objects.equals(user.getId(), shelter.getUser().getId())) {
      throw new NoAuthorizedException();
    }
  }

  /**
   * 보호소가 승인되지 않은 상태면 예외를 발생한다.
   *
   * @param shelter 보호소
   */
  private void throwIfShelterNotVerified(Shelter shelter) {
    if (shelter != null && !shelter.isDeleted() && !shelter.isVerified()) {
      throw new NotVerifiedShelterExistsException();
    }
  }

  /**
   * 사용자가 이미 승인된 보호소를 등록한 상태라면 예외를 발생한다.
   *
   * @param user 사용자
   */
  private void throwIfUserAlreadyRegisterShelter(User user) {
    Shelter shelter = user.getShelter();
    if (shelter != null && shelter.isVerified()) {
      throw new UserAlreadyRegisterShelterException();
    }
  }

  /**
   * 사용자가 ADMIN 권한이 없으면 예외를 발생한다.
   *
   * @param user 사용자
   */
  private void throwIfUserHasNotAdminRole(User user) {
    // TODO: 권한 제어 설정 후 삭제 확인
    if (!user.getRole().contains(ADMIN)) {
      throw new NoAuthorizedException();
    }
  }

  /**
   * 보호소가 이미 인증된 상태라면 예외를 발생한다.
   *
   * @param shelter 보호소
   */
  private void throwIfShelterAlreadyVerified(Shelter shelter) {
    if (shelter.isVerified()) {
      throw new ShelterAlreadyVerifiedException(shelter.getId());
    }
  }

  /**
   * 사용자를 반환한다.
   *
   * @param userId 사용자 아이디
   * @return 사용자
   */
  private User findByUserIdOrThrow(long userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }

  /**
   * 삭제되지 않은 보호소를 반환한다.
   *
   * @param shelterId 보호소 아이디
   * @return 보호소
   */
  private Shelter findByShelterIdOrThrow(long shelterId) {
    Shelter shelter = shelterRepository.findById(shelterId)
        .orElseThrow(() -> new ShelterNotFoundException(shelterId));

    if (shelter.isDeleted()) {
      throw new ShelterNotFoundException(shelterId);
    }

    return shelter;
  }

  /**
   * 보호소를 등록하거나 복구한다.
   *
   * @param user         사용자
   * @param request      보호소 등록 요청
   * @param shelterImage 보호소 이미지
   */
  private void registerOrRestoreShelter(User user, ShelterRegisterRequest request,
                                        S3Object shelterImage) {
    Shelter shelter = user.getShelter();
    String imageUrl = shelterImage == null ? null : shelterImage.getUrl();

    if (shelter == null) {
      shelter = Shelter.builder()
          .name(request.getName())
          .description(request.getDescription())
          .tel(request.getTel())
          .address(request.getAddress())
          .latitude(request.getLatitude())
          .longitude(request.getLongitude())
          .shelterImgUrl(imageUrl)
          .verified(false)
          .user(user)
          .build();
    }
    user.registerShelter(shelter);
  }

  /**
   * 보호소 이미지 업로드
   *
   * @param shelterImage 이미지
   * @return 업로드된 객체
   */
  private S3Object uploadShelterImage(MultipartFile shelterImage) {
    if (shelterImage != null && !shelterImage.isEmpty()) {
      return S3Object.from(s3Service.uploadOneFile(shelterImage, FilePath.SHELTER));
    }
    return null;
  }

  /**
   * 보호소에 등록된 이미지가 있으면 삭제한다
   *
   * @param oldShelterImage 이미지
   */
  private void deleteOldShelterImageIfExists(S3Object oldShelterImage) {
    if (oldShelterImage == null) {
      return;
    }

    try {
      s3Service.deleteFile(oldShelterImage.getFileName());
    } catch (Exception e) {
      // TODO: 삭제 못한 이미지에 대한 예외 처리
      log.error("delete shelter image failed. file={}", oldShelterImage, e);
    }
  }
}
