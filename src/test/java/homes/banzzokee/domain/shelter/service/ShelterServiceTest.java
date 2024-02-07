package homes.banzzokee.domain.shelter.service;

import static homes.banzzokee.domain.type.Role.ROLE_ADMIN;
import static homes.banzzokee.domain.type.Role.ROLE_SHELTER;
import static homes.banzzokee.domain.type.Role.ROLE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.dto.ShelterRegisterRequest;
import homes.banzzokee.domain.shelter.dto.ShelterUpdateRequest;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.shelter.exception.NotVerifiedShelterExistsException;
import homes.banzzokee.domain.shelter.exception.ShelterAlreadyVerifiedException;
import homes.banzzokee.domain.shelter.exception.ShelterNotFoundException;
import homes.banzzokee.domain.shelter.exception.UserAlreadyRegisterShelterException;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.infra.fileupload.dto.FileDto;
import homes.banzzokee.infra.fileupload.service.FileUploadService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ShelterServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ShelterRepository shelterRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private FileUploadService s3Service;

  @InjectMocks
  private ShelterService shelterService;

  private final MultipartFile mockFile = MockDataUtil.createMockMultipartFile(
      "shelterImg",
      "src/test/resources/images/banzzokee.png");
  private static final ShelterRegisterRequest shelterRegisterRequest
      = ShelterRegisterRequest.builder()
      .name("보호소")
      .description("설명")
      .tel("02-1234-5678")
      .address("주소")
      .latitude(24.0)
      .longitude(37.0)
      .build();
  private static final ShelterUpdateRequest shelterUpdateRequest
      = ShelterUpdateRequest.builder()
      .name("name")
      .description("description")
      .tel("02-1234-5678")
      .address("address")
      .latitude(1.0)
      .longitude(2.0)
      .build();

  private static final FileDto image = FileDto.builder()
      .url("url")
      .filename("filename")
      .build();

  ShelterServiceTest() throws IOException {
  }

  @Test
  @DisplayName("[보호소 등록] - 사용자를 못찾으면 UserNotFoundException 발생")
  void registerShelter_when_userNotExists_then_throwUserNotFoundException() {
    // given
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(UserNotFoundException.class,
        () -> shelterService.registerShelter(shelterRegisterRequest,
            mockFile,
            anyLong()));
  }

  @Test
  @DisplayName("[보호소 등록] - 승인되지 않은 보호소가 존재하는 경우 NotVerifiedShelterExistsException 발생")
  void registerShelter_when_ShelterIsNotVerified_then_throwNotVerifiedShelterExistsException() {
    // given
    Shelter shelter = mock(Shelter.class);
    given(shelter.isVerified()).willReturn(false);

    User user = mock(User.class);
    given(user.getShelter()).willReturn(shelter);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

    // when & then
    assertThrows(NotVerifiedShelterExistsException.class,
        () -> shelterService.registerShelter(shelterRegisterRequest,
            mockFile,
            anyLong()));
  }

  @Test
  @DisplayName("[보호소 등록] - 등록된 보호소가 있으면 UserAlreadyRegisterShelterException 발생")
  void registerShelter_when_hasShelter_then_throwUserAlreadyRegisterShelterException() {
    // given
    Shelter shelter = mock(Shelter.class);
    given(shelter.isVerified()).willReturn(true);

    User user = mock(User.class);
    given(user.getShelter()).willReturn(shelter);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

    // when & then
    assertThrows(UserAlreadyRegisterShelterException.class,
        () -> shelterService.registerShelter(shelterRegisterRequest,
            mockFile,
            anyLong()));
  }

  @Test
  @DisplayName("[보호소 등록] - 성공 검증, 새로운 보호소 등록")
  void registerShelter_success_verify_when_registerNew() {
    // given
    User user = mock(User.class);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(s3Service.uploadOneFile(any(MultipartFile.class), any(FilePath.class))).willReturn(image);

    // when
    shelterService.registerShelter(shelterRegisterRequest, mockFile, anyLong());

    // then
    // 이미지 업로드 검증
    ArgumentCaptor<MultipartFile> shelterImgCaptor
        = ArgumentCaptor.forClass(MultipartFile.class);
    verify(s3Service).uploadOneFile(shelterImgCaptor.capture(), any(FilePath.class));
    assertEquals(mockFile.getSize(), shelterImgCaptor.getValue().getSize());
    assertEquals(mockFile.getName(), shelterImgCaptor.getValue().getName());

    // 보호소 저장 검증
    ArgumentCaptor<Shelter> shelterCaptor = ArgumentCaptor.forClass(Shelter.class);
    verify(user).registerShelter(shelterCaptor.capture());
    Shelter shelter = shelterCaptor.getValue();
    assertEquals(shelterRegisterRequest.getName(), shelter.getName());
    assertEquals(shelterRegisterRequest.getDescription(), shelter.getDescription());
    assertEquals(shelterRegisterRequest.getTel(), shelter.getTel());
    assertEquals(shelterRegisterRequest.getAddress(), shelter.getAddress());
    assertEquals(shelterRegisterRequest.getLatitude(), shelter.getLatitude());
    assertEquals(shelterRegisterRequest.getLongitude(), shelter.getLongitude());
    assertEquals(image.getUrl(), shelter.getShelterImageUrl());
    assertFalse(shelter.isVerified());
  }

  @Test
  @DisplayName("[보호소 등록] - 성공 검증, 삭제된 보호소 복원")
  void registerShelter_success_verify_when_restoreShelter() {
    // given
    Shelter shelter = spy(Shelter.builder()
        .user(User.builder().build())
        .build());
    given(shelter.getId()).willReturn(1L);
    given(shelter.isDeleted()).willReturn(true);

    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    given(user.getId()).willReturn(1L);
    given(userRepository.findById(shelter.getId())).willReturn(Optional.of(user));

    given(s3Service.uploadOneFile(any(MultipartFile.class), any(FilePath.class))).willReturn(image);

    // when
    shelterService.registerShelter(shelterRegisterRequest, mockFile, user.getId());

    // then
    // 이미지 업로드 검증
    ArgumentCaptor<MultipartFile> shelterImgCaptor = ArgumentCaptor.forClass(
        MultipartFile.class);
    verify(s3Service).uploadOneFile(shelterImgCaptor.capture(), any(FilePath.class));
    assertEquals(mockFile.getSize(), shelterImgCaptor.getValue().getSize());

    // 보호소 복구 검증
    verify(user).registerShelter(shelter);
    verify(shelter).restore();
    assertFalse(shelter.isVerified());
    assertNull(shelter.getDeletedAt());
  }

  @Test
  @DisplayName("[보호소 승인] - 호출자가 ADMIN 권한이 없으면 NoAuthorizedException 발생")
  void verifyShelter_when_userHasNotAdminRole_then_throwNoAuthorizedException() {
    // given
    User user = mock(User.class);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

    // when & then
    assertThrows(NoAuthorizedException.class,
        () -> shelterService.verifyShelter(1L, anyLong()));
  }

  @Test
  @DisplayName("[보호소 승인] - 보호소를 찾을 수 없으면 ShelterNotFoundException 발생")
  void verifyShelter_when_shelterNotExists_then_throwShelterNotFoundException() {
    // given
    User user = mock(User.class);
    given(user.getRole()).willReturn(getRoles(ROLE_ADMIN));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(shelterRepository.findById(1L)).willReturn(Optional.empty());

    // when & then
    assertThrows(ShelterNotFoundException.class,
        () -> shelterService.verifyShelter(1L, anyLong()));
  }

  @Test
  @DisplayName("[보호소 승인] - 삭제된 보호소이면 ShelterNotFoundException 발생")
  void verifyShelter_when_shelterIsDeleted_then_throwShelterNotFoundException() {
    // given
    User user = mock(User.class);
    given(user.getRole()).willReturn(getRoles(ROLE_ADMIN));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

    Shelter shelter = mock(Shelter.class);
    given(shelter.isDeleted()).willReturn(true);
    given(shelterRepository.findById(1L)).willReturn(Optional.of(shelter));

    // when & then
    assertThrows(ShelterNotFoundException.class,
        () -> shelterService.verifyShelter(1L, user.getId()));
  }

  @Test
  @DisplayName("[보호소 승인] - 승인된 보호소면 ShelterAlreadyVerifiedException 발생")
  void verifyShelter_when_shelterVerified_then_throwShelterAlreadyVerifiedException() {
    // given
    User user = mock(User.class);
    given(user.getRole()).willReturn(getRoles(ROLE_ADMIN));
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    Shelter shelter = mock(Shelter.class);
    given(shelter.isVerified()).willReturn(true);
    given(shelterRepository.findById(shelter.getId())).willReturn(Optional.of(shelter));

    // when & then
    assertThrows(ShelterAlreadyVerifiedException.class,
        () -> shelterService.verifyShelter(shelter.getId(), user.getId()));
  }

  @Test
  @DisplayName("[보호소 승인] - 성공 검증")
  void verifyShelter_success_verify() {
    // given
    User user = spy(User.builder()
        .role(getRoles(ROLE_ADMIN))
        .build());
    given(user.getId()).willReturn(1L);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    Shelter shelter = spy(Shelter.builder().user(user).build());
    given(shelter.getId()).willReturn(1L);
    given(shelterRepository.findById(shelter.getId())).willReturn(Optional.of(shelter));

    // when
    shelterService.verifyShelter(shelter.getId(), user.getId());

    // then
    verify(shelter).verify();
    assertTrue(shelter.isVerified());
    assertTrue(user.getRole().contains(ROLE_SHELTER));
  }

  @Test
  @DisplayName("[보호소 수정] - 보호소를 찾을 수 없으면 ShelterNotFoundException 발생")
  void updateShelter_when_shelterNotExists_then_throwShelterNotFoundException() {
    // given
    User user = mock(User.class);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    Shelter shelter = mock(Shelter.class);
    given(shelterRepository.findById(shelter.getId())).willReturn(Optional.empty());

    // when & then
    assertThrows(ShelterNotFoundException.class,
        () -> shelterService.updateShelter(shelter.getId(),
            shelterUpdateRequest,
            mockFile,
            user.getId()));
  }

  @Test
  @DisplayName("[보호소 수정] - 사용자가 보호소를 등록한 사용자가 아니면 NoAuthorizedException 발생")
  void updateShelter_when_userIsNotShelterUser_then_throwNoAuthorizedException() {
    // given
    Shelter shelter = mock(Shelter.class);
    given(shelter.getUser()).willReturn(mock(User.class));
    given(shelterRepository.findById(shelter.getId())).willReturn(Optional.of(shelter));

    User user = mock(User.class);
    given(user.getId()).willReturn(1L);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when & then
    assertThrows(NoAuthorizedException.class,
        () -> shelterService.updateShelter(shelter.getId(),
            shelterUpdateRequest,
            mockFile,
            user.getId()));
  }

  @Test
  @DisplayName("[보호소 수정] - 성공 검증, 이전 이미지가 null이 아닌 경우 삭제한다")
  void updateShelter_when_oldShelterImageNotNull_then_deleteOldShelterImage() {
    // given
    S3Object oldShelterImage = new S3Object("oldShelterImage.png");

    User user = mock(User.class);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    Shelter shelter = mock(Shelter.class);
    given(shelter.getUser()).willReturn(user);
    given(shelter.getShelterImage()).willReturn(oldShelterImage);
    given(shelterRepository.findById(shelter.getId())).willReturn(Optional.of(shelter));

    // when
    shelterService.updateShelter(shelter.getId(),
        shelterUpdateRequest,
        mockFile,
        user.getId());

    // then
    verify(s3Service).deleteFile(oldShelterImage.getFileName());
  }

  @Test
  @DisplayName("[보호소 수정] - 성공 검증, 이전 이미지가 null이 아닌 경우 삭제한다")
  void updateShelter_when_shelterImageNotNull_then_uploadShelterImage() {
    // given
    User user = mock(User.class);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    Shelter shelter = mock(Shelter.class);
    given(shelter.getUser()).willReturn(user);
    given(shelterRepository.findById(shelter.getId())).willReturn(Optional.of(shelter));

    // when
    shelterService.updateShelter(shelter.getId(),
        shelterUpdateRequest,
        mockFile,
        user.getId());

    // then
    verify(s3Service).uploadOneFile(mockFile, FilePath.SHELTER);
  }

  @Test
  @DisplayName("[보호소 수정] - 성공 검증")
  void updateShelter_when_success_then_verify() {
    // given
    User user = mock(User.class);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    Shelter shelter = spy(Shelter.builder()
        .user(user)
        .build());
    given(shelter.getId()).willReturn(1L);
    given(shelter.getUser()).willReturn(user);
    given(shelterRepository.findById(shelter.getId())).willReturn(Optional.of(shelter));

    given(s3Service.uploadOneFile(mockFile, FilePath.SHELTER)).willReturn(image);

    // when
    shelterService.updateShelter(shelter.getId(),
        shelterUpdateRequest,
        mockFile,
        user.getId());

    // then
    assertEquals(shelterUpdateRequest.getName(), shelter.getName());
    assertEquals(shelterUpdateRequest.getDescription(), shelter.getDescription());
    assertEquals(shelterUpdateRequest.getTel(), shelter.getTel());
    assertEquals(shelterUpdateRequest.getAddress(), shelter.getAddress());
    assertEquals(shelterUpdateRequest.getLatitude(), shelter.getLatitude());
    assertEquals(shelterUpdateRequest.getLongitude(), shelter.getLongitude());
    assertEquals(image.getUrl(), shelter.getShelterImageUrl());
  }

  @Test
  @DisplayName("[보호소 삭제] - 보호소를 찾을 수 없으면 ShelterNotFoundException 발생")
  void unregisterShelter_when_shelterNotExists_then_throwShelterNotFoundException() {
    // given
    User user = mock(User.class);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(shelterRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(ShelterNotFoundException.class,
        () -> shelterService.unregisterShelter(1L, user.getId()));
  }

  @Test
  @DisplayName("[보호소 삭제] - 삭제하는 사용자가 보호소를 등록한 사용자가 아니라면 NoAuthorizedException 발생")
  void unregisterShelter_when_userIsNotShelterUser_then_throwNoAuthorizedException() {
    // given
    User user1 = mock(User.class);
    given(user1.getId()).willReturn(1L);
    given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));

    User user2 = mock(User.class);
    given(user2.getId()).willReturn(2L);

    Shelter shelter = mock(Shelter.class);
    given(shelter.getUser()).willReturn(user2);
    given(shelterRepository.findById(anyLong())).willReturn(Optional.of(shelter));

    // when & then
    assertThrows(NoAuthorizedException.class,
        () -> shelterService.unregisterShelter(shelter.getId(), user1.getId()));
  }

  @Test
  @DisplayName("[보호소 삭제] - 성공 검증")
  void unregisterShelter_when_success_then_verify() {
    // given
    Shelter shelter = spy(Shelter.builder()
        .user(User.builder().build())
        .build());
    given(shelter.getId()).willReturn(1L);
    given(shelterRepository.findById(shelter.getId())).willReturn(Optional.of(shelter));

    User user = spy(User.builder()
        .role(getRoles(ROLE_USER, ROLE_SHELTER))
        .shelter(shelter)
        .build());
    given(user.getId()).willReturn(1L);
    given(shelter.getUser()).willReturn(user);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    List<ChatRoom> chatRooms = getMockChatRooms(shelter);
    given(chatRoomRepository.findAllByShelterId(shelter.getId())).willReturn(chatRooms);

    // when
    shelterService.unregisterShelter(shelter.getId(), user.getId());

    // then
    verify(user).unregisterShelter();
    assertFalse(user.getRole().contains(ROLE_SHELTER));

    verify(shelter).delete();
    assertTrue(shelter.isDeleted());
    assertFalse(shelter.isVerified());

    for (ChatRoom chatRoom : chatRooms) {
      verify(chatRoom).leaveShelter();
      assertNull(chatRoom.getShelter());
    }
  }

  private List<ChatRoom> getMockChatRooms(Shelter shelter) {
    return IntStream.range(0, 5)
        .mapToObj(m -> spy(ChatRoom.builder()
            .shelter(shelter)
            .build()))
        .toList();
  }

  private Set<Role> getRoles(Role... roles) {
    return Arrays.stream(roles).collect(Collectors.toSet());
  }
}