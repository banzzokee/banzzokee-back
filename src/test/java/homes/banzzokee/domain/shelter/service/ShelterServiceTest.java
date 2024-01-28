package homes.banzzokee.domain.shelter.service;

import static homes.banzzokee.domain.type.Role.ADMIN;
import static homes.banzzokee.domain.type.Role.SHELTER;
import static homes.banzzokee.domain.type.Role.USER;
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
import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.CustomException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.infra.fileupload.dto.ImageDto;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
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

  private final User mockUser = mock(User.class);
  private final User mockShelterUser = mock(User.class);
  private final Shelter mockShelter = mock(Shelter.class);
  private final MultipartFile mockFile = MockDataUtil.createMockMultipartFile(
      "shelterImg",
      "src/test/resources/images/banzzokee.png");
  private final Set<Role> adminRole = new HashSet<>(Collections.singleton(ADMIN));
  private final ShelterRegisterRequest mockShelterRegisterRequest = mock(
      ShelterRegisterRequest.class);
  private final ShelterUpdateRequest mockShelterUpdateRequest
      = mock(ShelterUpdateRequest.class);

  ShelterServiceTest() throws IOException {
  }

  @BeforeEach
  public void setup() {
    given(mockUser.getId()).willReturn(1L);

    given(mockShelterUser.getId()).willReturn(2L);
    given(mockShelterUser.getShelter()).willReturn(mockShelter);

    given(mockShelter.getId()).willReturn(1L);
    given(mockShelter.getUser()).willReturn(mockShelterUser);
  }


  @Test
  @DisplayName("[보호소 등록] - 사용자를 못찾으면 UserNotFoundException 발생")
  void registerShelter_when_userNotExists_then_throwUserNotFoundException() {
    // given
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.empty());

    // when
    // then
    assertThrows(UserNotFoundException.class,
        () -> shelterService.registerShelter(mockShelterRegisterRequest, mockFile,
            mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 등록] - 승인되지 않은 보호소가 존재하는 경우 NotVerifiedShelterExistsException 발생")
  void registerShelter_when_ShelterIsNotVerified_then_throwNotVerifiedShelterExistsException() {
    // given
    Shelter mockShelter = mock(Shelter.class);
    given(mockShelter.isVerified()).willReturn(false);

    given(mockUser.getShelter()).willReturn(mockShelter);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));

    // when
    // then
    assertThrows(NotVerifiedShelterExistsException.class,
        () -> shelterService.registerShelter(mockShelterRegisterRequest,
            mockFile,
            mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 등록] - 등록된 보호소가 있으면 UserAlreadyRegisterShelterException 발생")
  void registerShelter_when_hasShelter_then_throwUserAlreadyRegisterShelterException() {
    // given
    Shelter mockShelter = mock(Shelter.class);
    given(mockShelter.isVerified()).willReturn(true);

    given(mockUser.getShelter()).willReturn(mockShelter);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));

    // when
    // then
    assertThrows(UserAlreadyRegisterShelterException.class,
        () -> shelterService.registerShelter(mockShelterRegisterRequest, mockFile,
            mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 등록] - 성공 검증")
  void registerShelter_success_verify() {
    // given
    given(mockUser.hasShelter()).willReturn(false);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));

    ImageDto image = ImageDto.builder().url("url").filename("filename").build();
    given(s3Service.uploadOneFile(any(MultipartFile.class))).willReturn(image);

    // when
    ShelterRegisterRequest request = ShelterRegisterRequest.builder().name("보호소")
        .description("설명").tel("02-1234-5678").address("주소").latitude(24.0)
        .longitude(37.0).build();

    shelterService.registerShelter(request, mockFile, mockUser.getId());

    // then
    // 이미지 업로드 검증
    ArgumentCaptor<MultipartFile> shelterImgCaptor = ArgumentCaptor.forClass(
        MultipartFile.class);
    verify(s3Service).uploadOneFile(shelterImgCaptor.capture());
    assertEquals(mockFile.getSize(), shelterImgCaptor.getValue().getSize());

    // 보호소 저장 검증
    ArgumentCaptor<Shelter> shelterCaptor = ArgumentCaptor.forClass(Shelter.class);
    verify(mockUser).registerShelter(shelterCaptor.capture());
    Shelter shelter = shelterCaptor.getValue();
    assertEquals(request.getName(), shelter.getName());
    assertEquals(request.getDescription(), shelter.getDescription());
    assertEquals(request.getTel(), shelter.getTel());
    assertEquals(request.getAddress(), shelter.getAddress());
    assertEquals(request.getLatitude(), shelter.getLatitude());
    assertEquals(request.getLongitude(), shelter.getLongitude());
    assertEquals(image.getUrl(), shelter.getShelterImageUrl());
    assertFalse(shelter.isVerified());
  }

  @Test
  @DisplayName("[보호소 승인] - 호출자가 ADMIN 권한이 없으면 CustomException 발생")
  void verifyShelter_when_userHasNotAdminRole_then_throwCustomException() {
    // given
    given(mockUser.getRole()).willReturn(Collections.emptySet());
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));

    // when
    // then
    assertThrows(CustomException.class,
        () -> shelterService.verifyShelter(1L, mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 승인] - 보호소를 찾을 수 없으면 ShelterNotFoundException 발생")
  void verifyShelter_when_shelterNotExists_then_throwShelterNotFoundException() {
    // given
    given(mockUser.getRole()).willReturn(adminRole);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
    given(shelterRepository.findById(mockShelter.getId())).willReturn(Optional.empty());

    // when
    // then
    assertThrows(ShelterNotFoundException.class,
        () -> shelterService.verifyShelter(mockShelter.getId(), mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 승인] - 승인된 보호소면 ShelterAlreadyVerifiedException 발생")
  void verifyShelter_when_shelterVerified_then_throwShelterAlreadyVerifiedException() {
    // given
    given(mockUser.getRole()).willReturn(adminRole);
    given(mockShelter.isVerified()).willReturn(true);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
    given(shelterRepository.findById(mockShelter.getId()))
        .willReturn(Optional.of(mockShelter));

    // when
    // then
    assertThrows(ShelterAlreadyVerifiedException.class,
        () -> shelterService.verifyShelter(mockShelter.getId(), mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 승인] - 성공 검증")
  void verifyShelter_success_verify() {
    // given
    given(mockUser.getRole()).willReturn(adminRole);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));

    Set<Role> roles = new HashSet<>();
    roles.add(USER);

    User user = spy(User.builder()
        .role(roles)
        .build());
    Shelter shelter = spy(Shelter.builder()
        .user(user)
        .build());

    given(shelterRepository.findById(mockShelter.getId()))
        .willReturn(Optional.of(shelter));

    // when
    shelterService.verifyShelter(mockShelter.getId(), mockUser.getId());

    // then
    verify(shelter).verify();
    assertTrue(shelter.isVerified());
    assertTrue(user.getRole().contains(SHELTER));
    assertTrue(user.getRole().contains(USER));
  }

  @Test
  @DisplayName("[보호소 수정] - 보호소를 찾을 수 없으면 ShelterNotFoundException 발생")
  void updateShelter_when_shelterNotExists_then_throwShelterNotFoundException() {
    // given
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
    given(shelterRepository.findById(mockShelter.getId()))
        .willReturn(Optional.empty());

    // when
    // then
    assertThrows(ShelterNotFoundException.class,
        () -> shelterService.updateShelter(mockShelter.getId(),
            mockShelterUpdateRequest,
            mockFile,
            mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 수정] - 사용자가 보호소를 등록한 사용자가 아니면 NoAuthorizedException 발생")
  void updateShelter_when_userIsNotShelterUser_then_throwNoAuthorizedException() {
    // given
    given(mockShelter.getUser()).willReturn(mock(User.class));
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
    given(shelterRepository.findById(mockShelter.getId()))
        .willReturn(Optional.of(mockShelter));

    // when
    // then
    assertThrows(NoAuthorizedException.class,
        () -> shelterService.updateShelter(mockShelter.getId(),
            mockShelterUpdateRequest,
            mockFile,
            mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 수정] - 성공 검증, 이전 이미지가 null이 아닌 경우 삭제한다")
  void updateShelter_when_oldShelterImageNotNull_then_deleteOldShelterImage() {
    // given
    S3Object oldShelterImage = new S3Object("oldShelterImage.png");
    given(mockShelter.getUser()).willReturn(mockUser);
    given(mockShelter.getShelterImage()).willReturn(oldShelterImage);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
    given(shelterRepository.findById(mockShelter.getId()))
        .willReturn(Optional.of(mockShelter));

    // when
    shelterService.updateShelter(mockShelter.getId(),
        mockShelterUpdateRequest,
        mockFile,
        mockUser.getId());

    // then
    verify(s3Service).deleteFile(oldShelterImage.getFileName());
  }

  @Test
  @DisplayName("[보호소 수정] - 성공 검증, 이전 이미지가 null이 아닌 경우 삭제한다")
  void updateShelter_when_shelterImageNotNull_then_uploadShelterImage() {
    // given
    given(mockShelter.getUser()).willReturn(mockUser);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
    given(shelterRepository.findById(mockShelter.getId()))
        .willReturn(Optional.of(mockShelter));

    // when
    shelterService.updateShelter(mockShelter.getId(),
        mockShelterUpdateRequest,
        mockFile,
        mockUser.getId());

    // then
    verify(s3Service).uploadOneFile(mockFile);
  }

  @Test
  @DisplayName("[보호소 수정] - 성공 검증")
  void updateShelter_when_success_then_verify() {
    // given
    Shelter shelter = spy(Shelter.builder()
        .user(mockUser)
        .build());

    given(mockShelter.getUser()).willReturn(mockUser);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
    given(shelterRepository.findById(mockShelter.getId()))
        .willReturn(Optional.of(shelter));
    given(s3Service.uploadOneFile(mockFile))
        .willReturn(ImageDto.builder()
            .filename("filename")
            .url("url")
            .build());

    // when
    ShelterUpdateRequest request = ShelterUpdateRequest.builder()
        .name("name")
        .description("description")
        .tel("02-1234-5678")
        .address("address")
        .latitude(1.0)
        .longitude(2.0)
        .build();

    shelterService.updateShelter(mockShelter.getId(),
        request,
        mockFile,
        mockUser.getId());

    // then
    assertEquals(request.getName(), shelter.getName());
    assertEquals(request.getDescription(), shelter.getDescription());
    assertEquals(request.getTel(), shelter.getTel());
    assertEquals(request.getAddress(), shelter.getAddress());
    assertEquals(request.getLatitude(), shelter.getLatitude());
    assertEquals(request.getLongitude(), shelter.getLongitude());
    assertEquals("url", shelter.getShelterImageUrl());
  }

  @Test
  @DisplayName("[보호소 삭제] - 보호소를 찾을 수 없으면 ShelterNotFoundException 발생")
  void unregisterShelter_when_shelterNotExists_then_throwShelterNotFoundException() {
    // given
    given(userRepository.findById(mockUser.getId()))
        .willReturn(Optional.of(mockUser));
    given(shelterRepository.findById(mockShelter.getId()))
        .willReturn(Optional.empty());

    // when
    // then
    assertThrows(ShelterNotFoundException.class,
        () -> shelterService.unregisterShelter(mockShelter.getId(), mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 삭제] - 삭제하는 사용자가 보호소를 등록한 사용자가 아니라면 NoAuthorizedException 발생")
  void unregisterShelter_when_userIsNotShelterUser_then_throwNoAuthorizedException() {
    // given
    User user1 = mock(User.class);
    given(user1.getId()).willReturn(1L);

    User user2 = mock(User.class);
    given(user2.getId()).willReturn(2L);

    Shelter shelter = mock(Shelter.class);
    given(shelter.getUser()).willReturn(user2);

    given(userRepository.findById(anyLong()))
        .willReturn(Optional.of(user1));
    given(shelterRepository.findById(anyLong()))
        .willReturn(Optional.of(shelter));

    // when
    // then
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

    Set<Role> roles = new HashSet<>(Arrays.asList(USER, SHELTER));
    User user = spy(User.builder()
        .role(roles)
        .shelter(shelter)
        .build());
    given(user.getId()).willReturn(1L);
    given(shelter.getUser()).willReturn(user);

    given(userRepository.findById(shelter.getId()))
        .willReturn(Optional.of(user));
    given(shelterRepository.findById(user.getId()))
        .willReturn(Optional.of(shelter));

    List<ChatRoom> chatRooms = getMockChatRooms(shelter);
    given(chatRoomRepository.findAllByShelterId(shelter.getId()))
        .willReturn(chatRooms);

    // when
    shelterService.unregisterShelter(shelter.getId(), user.getId());

    // then
    verify(user).unregisterShelter();
    assertFalse(user.getRole().contains(SHELTER));

    verify(shelter).delete();
    assertTrue(shelter.isDeleted());
    assertFalse(shelter.isVerified());

    for (ChatRoom chatRoom: chatRooms) {
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
}