package homes.banzzokee.domain.adoption.service;

import static homes.banzzokee.domain.type.AdoptionStatus.ADOPTING;
import static homes.banzzokee.domain.type.AdoptionStatus.FINISHED;
import static homes.banzzokee.domain.type.AdoptionStatus.RESERVING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import homes.banzzokee.domain.adoption.elasticsearch.document.subclass.UserDocumentVo;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionDocumentNotFoundException;
import homes.banzzokee.domain.adoption.exception.AdoptionIsDeletedException;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.adoption.exception.AlreadyFinishedAdoptionException;
import homes.banzzokee.domain.adoption.exception.AssignedUserMustBeNullException;
import homes.banzzokee.domain.adoption.exception.CurrentStatusIsSameToChangeException;
import homes.banzzokee.domain.adoption.exception.MustInputAssignedUserInfoException;
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
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.infra.fileupload.dto.FileDto;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AdoptionServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private FileUploadService fileUploadService;
  @Mock
  private AdoptionRepository adoptionRepository;
  @Mock
  private AdoptionSearchRepository adoptionSearchRepository;
  @Mock
  private AdoptionSearchQueryRepository queryRepository;
  @InjectMocks
  private AdoptionService adoptionService;

  private final AdoptionRegisterRequest registerRequest = AdoptionRegisterRequest.builder()
      .title("강아지")
      .content("귀여운 강아지를 소개합니다.")
      .breed(BreedType.POMERANIAN)
      .size(DogSize.MEDIUM)
      .neutering(false)
      .gender(DogGender.MALE)
      .age(10)
      .healthChecked(true)
      .registeredAt("2024-01-01")
      .build();

  private final List<MultipartFile> images = createImageList(4);

  private final AdoptionUpdateRequest updateRequest = AdoptionUpdateRequest.builder()
      .title("강아지")
      .content("귀여운 강아지를 소개합니다.")
      .breed(BreedType.POMERANIAN)
      .size(DogSize.MEDIUM)
      .neutering(false)
      .gender(DogGender.MALE)
      .age(10)
      .healthChecked(true)
      .registeredAt("2024-01-01")
      .build();

  AdoptionServiceTest() throws IOException {
  }

  @Test
  @DisplayName("분양게시글 등록 성공 테스트")
  void registerAdoption_success() {
    //given
    Shelter shelter = spy(Shelter.builder()
        .description("행복한 보호소")
        .verified(true)
        .user(mock(User.class))
        .build());
    User user = spy(User.builder()
        .email("abcd@abcd.com")
        .shelter(shelter)
        .build());

    List<FileDto> fileDtoList = createFileDtoList(4);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(fileUploadService.uploadManyFile(anyList(), any(FilePath.class)))
        .willReturn(fileDtoList);
    given(adoptionRepository.save(any(Adoption.class))).will(returnsFirstArg());
    given(user.getCreatedAt()).willReturn(LocalDateTime.now());
    given(shelter.getCreatedAt()).willReturn(LocalDateTime.now());

    //when
    adoptionService.registerAdoption(registerRequest, images, 1L);

    //then
    ArgumentCaptor<AdoptionDocument> adoptionDocumentCaptor = ArgumentCaptor.forClass(
        AdoptionDocument.class);
    verify(adoptionSearchRepository).save(adoptionDocumentCaptor.capture());

    // ES 저장되는 AdoptionDocument 객체 검증
    assertEquals(user.getEmail(), adoptionDocumentCaptor.getValue().getUser().getEmail());
    assertEquals(user.getShelter().getDescription(),
        adoptionDocumentCaptor.getValue().getUser().getShelter().getDescription());
    assertEquals(registerRequest.getTitle(),
        adoptionDocumentCaptor.getValue().getTitle());
    assertEquals(registerRequest.getContent(),
        adoptionDocumentCaptor.getValue().getContent());
    assertEquals(registerRequest.getBreed(),
        adoptionDocumentCaptor.getValue().getBreed());
    assertEquals(registerRequest.getSize(), adoptionDocumentCaptor.getValue().getSize());
    assertEquals(registerRequest.isNeutering(),
        adoptionDocumentCaptor.getValue().isNeutering());
    assertEquals(registerRequest.getGender(),
        adoptionDocumentCaptor.getValue().getGender());
    assertEquals(registerRequest.getAge(), adoptionDocumentCaptor.getValue().getAge());
    assertEquals(registerRequest.isHealthChecked(),
        adoptionDocumentCaptor.getValue().isHealthChecked());
    assertEquals(fileDtoList.size(),
        adoptionDocumentCaptor.getValue().getImages().size());
    assertEquals(fileDtoList.get(0).getUrl(),
        adoptionDocumentCaptor.getValue().getImages().get(0).getUrl());
    assertEquals(fileDtoList.get(1).getUrl(),
        adoptionDocumentCaptor.getValue().getImages().get(1).getUrl());
    assertEquals(fileDtoList.get(2).getUrl(),
        adoptionDocumentCaptor.getValue().getImages().get(2).getUrl());
    assertEquals(fileDtoList.get(3).getUrl(),
        adoptionDocumentCaptor.getValue().getImages().get(3).getUrl());
    assertEquals(ADOPTING, adoptionDocumentCaptor.getValue().getStatus());
  }

  @Test
  @DisplayName("분양게시글 등록 - user가 존재하지 않으면 UserNotFoundException 발생")
  void registerAdoption_shouldThrowUserNotFoundException_whenUserIsNotExist() {
    //given
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());
    //when & then
    assertThrows(UserNotFoundException.class,
        () -> adoptionService.registerAdoption(registerRequest, images, 1L));
  }

  @Test
  @DisplayName("분양게시글 등록 - user에 등록된 shelter가 없으면 NoAuthorizedException")
  void registerAdoption_shouldThrowNoAuthorizedException_whenUserIsNotRegisteredShelter() {
    //given
    User user = User.builder()
        .email("abcd@abcd.com")
        .build();

    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.registerAdoption(registerRequest, images, 1L));
  }

  @Test
  @DisplayName("분양게시글 등록 - user에 등록된 shelter가 삭제된 경우 NoAuthorizedException")
  void registerAdoption_shouldThrowNoAuthorizedException_whenShelterIsDeleted() {
    //given
    Shelter shelter = Shelter.builder()
        .user(mock(User.class))
        .build();
    shelter.delete();
    User user = spy(User.builder()
        .email("abcd@abcd.com")
        .shelter(shelter)
        .build());

    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.registerAdoption(registerRequest, images, 1L));
  }

  @Test
  @DisplayName("분양게시글 등록 - 인증된 shelter가 아닌 경우 NotVerifiedShelterExistsException 발생")
  void regiserAdoption_shouldThrowNotVerifiedShelterExistsException_whenShelterIsNotVerified() {
    //given
    Shelter shelter = Shelter.builder()
        .user(mock(User.class))
        .verified(false)
        .build();
    User user = spy(User.builder()
        .email("abcd@abcd.com")
        .shelter(shelter)
        .build());

    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    //when & then
    assertThrows(NotVerifiedShelterExistsException.class,
        () -> adoptionService.registerAdoption(registerRequest, images, 1L));
  }

  @Test
  @DisplayName("분양게시글 상세정보 조회 성공 테스트")
  void successGetAdoption() {
    //given
    User user = spy(User.builder()
        .build());
    Adoption adoption = Adoption.builder()
        .title("강아지")
        .breed(BreedType.findByString("포메라니안"))
        .user(user)
        .status(ADOPTING)
        .build();
    LocalDateTime now = LocalDateTime.now();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getCreatedAt()).willReturn(now);
    //when
    AdoptionResponse response = adoptionService.getAdoption(2L);
    //then
    assertEquals("강아지", response.getTitle());
    assertEquals(BreedType.POMERANIAN, response.getBreed());
    assertEquals(ADOPTING, response.getStatus());
    assertEquals(now.toLocalDate(), response.getUser().getJoinedAt());
  }

  @Test
  @DisplayName("분양게시글 상세정보 조회 - 분양게시글 존재하지 않을 경우 예외 처리")
  void getAdoption_shouldThrowAdoptionNotFoundException_whenAdoptionIsNotExist() {
    //given
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());
    //when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> adoptionService.getAdoption(2L));
  }

  @Test
  @DisplayName("분양게시글 상세정보 조회 - 분양게시글 삭제된 경우 예외 처리")
  void getAdoption_shouldThrowAdoptionIsDeletedException_whenAdoptionIsDeleted() {
    //given
    User user = User.builder().build();
    Adoption adoption = spy(Adoption.builder()
        .title("강아지")
        .breed(BreedType.findByString("포메라니안"))
        .user(user)
        .status(ADOPTING)
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(adoption.getDeletedAt()).willReturn(LocalDateTime.now());
    //when & then
    assertThrows(AdoptionIsDeletedException.class,
        () -> adoptionService.getAdoption(2L));
  }

  @Test
  @DisplayName("분양게시글 수정 성공 테스트")
  void updateAdoption_success() {
    //given
    Shelter shelter = Shelter.builder()
        .verified(true)
        .user(mock(User.class))
        .build();
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(RESERVING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());
    AdoptionDocument adoptionDocument = AdoptionDocument.builder().build();
    List<FileDto> uploadedImages = createFileDtoList(4);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);
    given(fileUploadService.uploadManyFile(anyList(), any(FilePath.class)))
        .willReturn(uploadedImages);
    given(adoptionRepository.save(any(Adoption.class))).will(returnsFirstArg());
    given(adoption.getId()).willReturn(1L);
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.of(adoptionDocument));

    //when
    adoptionService.updateAdoption(1L, updateRequest, images, 1L);
    //then
    ArgumentCaptor<AdoptionDocument> adoptionDocumentArgumentCaptor = ArgumentCaptor.forClass(
        AdoptionDocument.class);
    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

    verify(adoptionSearchRepository).save(adoptionDocumentArgumentCaptor.capture());
    verify(fileUploadService, times(2)).deleteFile(stringCaptor.capture());

    assertEquals(updateRequest.getTitle(),
        adoptionDocumentArgumentCaptor.getValue().getTitle());
    assertEquals(updateRequest.getContent(),
        adoptionDocumentArgumentCaptor.getValue().getContent());
    assertEquals(updateRequest.getBreed(),
        adoptionDocumentArgumentCaptor.getValue().getBreed());
    assertEquals(updateRequest.getSize(),
        adoptionDocumentArgumentCaptor.getValue().getSize());
    assertEquals(updateRequest.isNeutering(),
        adoptionDocumentArgumentCaptor.getValue().isNeutering());
    assertEquals(updateRequest.getGender(),
        adoptionDocumentArgumentCaptor.getValue().getGender());
    assertEquals(updateRequest.getAge(),
        adoptionDocumentArgumentCaptor.getValue().getAge());
    assertEquals(updateRequest.isHealthChecked(),
        adoptionDocumentArgumentCaptor.getValue().isHealthChecked());
    assertEquals(LocalDate.parse(updateRequest.getRegisteredAt()),
        adoptionDocumentArgumentCaptor.getValue().getRegisteredAt());
    assertEquals(uploadedImages.size(),
        adoptionDocumentArgumentCaptor.getValue().getImages().size());
    assertEquals(uploadedImages.get(0).getUrl(),
        adoptionDocumentArgumentCaptor.getValue().getImages().get(0).getUrl());
    assertEquals(uploadedImages.get(1).getUrl(),
        adoptionDocumentArgumentCaptor.getValue().getImages().get(1).getUrl());
    assertEquals(uploadedImages.get(2).getUrl(),
        adoptionDocumentArgumentCaptor.getValue().getImages().get(2).getUrl());
    assertEquals(uploadedImages.get(3).getUrl(),
        adoptionDocumentArgumentCaptor.getValue().getImages().get(3).getUrl());

    assertEquals("url1", stringCaptor.getAllValues().get(0));
    assertEquals("url2", stringCaptor.getAllValues().get(1));
  }

  @Test
  @DisplayName("분양게시글 수정 - 분양게시글 존재하지 않을 경우 NotFoundAdoptionException")
  void updateAdoption_shouldThrowNotFoundAdoptionException_whenAdoptionIsNotExist() {
    //given
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());

    //when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> adoptionService.updateAdoption(1L, updateRequest, images, 1L));
  }

  @Test
  @DisplayName("분양게시글 수정 - 삭제된 분양게시글인 경우")
  void updateAdoption_shouldThrowAdoptionIsDeletedException_whenAdoptionIsDeleted() {
    //given
    User user = spy(User.builder().build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(FINISHED)
        .images(List.of(new S3Object("url")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(adoption.getDeletedAt()).willReturn(LocalDateTime.now());

    //when & then
    assertThrows(AdoptionIsDeletedException.class,
        () -> adoptionService.updateAdoption(1L, updateRequest, images, 1L));

  }

  @Test
  @DisplayName("분양게시글 수정 - 분양게시글 상태가 분양완료인 경우")
  void updateAdoption_shouldThrowAlreadyFinishedAdoption_whenAdoptionStatusIsFinished() {
    //given
    User user = spy(User.builder().build());
    Adoption adoption = Adoption.builder()
        .user(user)
        .status(FINISHED)
        .images(List.of(new S3Object("url")))
        .build();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    //when & then
    assertThrows(AlreadyFinishedAdoptionException.class,
        () -> adoptionService.updateAdoption(1L, updateRequest, images, 1L));

  }

  @Test
  @DisplayName("분양게시글 수정 - 작성자와 수정 요청자가 다를 경우")
  void updateAdoption_shouldThrowNoAuthorizedException_whenRequestUserIsNotMatchedAdoptionWriter() {
    //given
    User user = spy(User.builder().build());
    Adoption adoption = Adoption.builder()
        .user(user)
        .status(RESERVING)
        .images(List.of(new S3Object("url")))
        .build();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(2L);

    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.updateAdoption(1L, updateRequest, images, 1L));

  }

  @Test
  @DisplayName("분양게시글 수정 - 작성자의 보호소가 삭제된 경우")
  void updateAdoption_shouldThrowNoAuthorizedException_whenShelterIsDeleted() {
    //given
    Shelter shelter = spy(Shelter.builder()
        .verified(true)
        .user(mock(User.class))
        .build());
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = Adoption.builder()
        .user(user)
        .status(RESERVING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);
    given(shelter.isDeleted()).willReturn(true);

    //when
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.updateAdoption(1L, updateRequest, images, 1L));
  }

  @Test
  @DisplayName("분양게시글 수정 - 작성자의 보호소가 존재하지 않는 경우")
  void updateAdoption_shouldThrowNoAuthorizedException_whenShelterIsNotExist() {
    //given
    User user = spy(User.builder()
        .build());
    Adoption adoption = Adoption.builder()
        .user(user)
        .status(RESERVING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);

    //when
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.updateAdoption(1L, updateRequest, images, 1L));
  }

  @Test
  @DisplayName("분양게시글 수정 - 작성자의 보호소가 인증되지 않은 경우")
  void updateAdoption_shouldThrowNotVerifiedShelterExistException_whenShelterIsNotVerified() {
    //given
    Shelter shelter = spy(Shelter.builder()
        .verified(false)
        .user(mock(User.class))
        .build());
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = Adoption.builder()
        .user(user)
        .status(RESERVING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);

    //when
    assertThrows(NotVerifiedShelterExistsException.class,
        () -> adoptionService.updateAdoption(1L, updateRequest, images, 1L));
  }

  @Test
  @DisplayName("분양게시글 수정 - ES에 저장된 adoptionDcoument가 없을 경우")
  void updateAdoption_shouldThrowAdoptionDocumentNotFoundException_whenAdoptionDocumentIsNotExist() {
    //given
    Shelter shelter = Shelter.builder()
        .verified(true)
        .user(mock(User.class))
        .build();
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(RESERVING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    List<FileDto> uploadedImages = createFileDtoList(4);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);
    given(fileUploadService.uploadManyFile(anyList(), any(FilePath.class)))
        .willReturn(uploadedImages);
    given(adoptionRepository.save(any(Adoption.class))).will(returnsFirstArg());
    given(adoption.getId()).willReturn(1L);
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.empty());

    //when & then
    assertThrows(AdoptionDocumentNotFoundException.class,
        () -> adoptionService.updateAdoption(1L, updateRequest, images, 1L));

  }

  @Test
  @DisplayName("분양게시글 상태 분양완료로 변경 성공 테스트")
  void changeAdoptionStatus_success_whenToChangeFinished() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(FINISHED)
        .assignedUserId(5L)
        .build();
    Shelter shelter = Shelter.builder()
        .verified(true)
        .user(mock(User.class))
        .build();
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    User assignedUser = spy(User.builder().build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(AdoptionStatus.RESERVING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());
    AdoptionDocument adoptionDocument = AdoptionDocument.builder().build();
    LocalDateTime now = LocalDateTime.now();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(assignedUser));
    given(adoptionRepository.save(any(Adoption.class))).will(returnsFirstArg());
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.of(adoptionDocument));
    given(adoption.getId()).willReturn(2L);
    given(assignedUser.getId()).willReturn(5L);
    given(assignedUser.getCreatedAt()).willReturn(now);

    //when
    adoptionService.changeAdoptionStatus(2L, request, 1L);

    //then
    ArgumentCaptor<AdoptionDocument> adoptionDocumentArgumentCaptor =
        ArgumentCaptor.forClass(AdoptionDocument.class);
    verify(adoptionSearchRepository).save(adoptionDocumentArgumentCaptor.capture());

    assertEquals(request.getStatus(),
        adoptionDocumentArgumentCaptor.getValue().getStatus());
    assertNotNull(adoptionDocumentArgumentCaptor.getValue().getAdoptedAt());
    assertEquals(5L,
        adoptionDocumentArgumentCaptor.getValue().getAssignedUser().getUserId());
    assertEquals(now.toLocalDate(),
        adoptionDocumentArgumentCaptor.getValue().getAssignedUser().getJoinedAt());
  }

  @Test
  @DisplayName("분양게시글 상태 예약중으로 변경 성공 테스트")
  void changeAdoptionStatus_success_whenToChangeReserving() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .build();
    Shelter shelter = Shelter.builder()
        .verified(true)
        .user(mock(User.class))
        .build();
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());
    AdoptionDocument adoptionDocument = AdoptionDocument.builder().build();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);
    given(adoptionRepository.save(any(Adoption.class))).will(returnsFirstArg());
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.of(adoptionDocument));
    given(adoption.getId()).willReturn(2L);
    //when
    adoptionService.changeAdoptionStatus(2L, request, 1L);

    //then
    ArgumentCaptor<AdoptionDocument> adoptionDocumentArgumentCaptor =
        ArgumentCaptor.forClass(AdoptionDocument.class);
    verify(adoptionSearchRepository).save(adoptionDocumentArgumentCaptor.capture());

    assertEquals(request.getStatus(),
        adoptionDocumentArgumentCaptor.getValue().getStatus());
    assertNull(adoptionDocumentArgumentCaptor.getValue().getAdoptedAt());
    assertNull(adoptionDocumentArgumentCaptor.getValue().getAssignedUser());
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 분양완료로 변경하려는 경우 assignedUserId가 null인 경우")
  void changeAdoptionStatus_shouldThrowValidationError_whenChangeToFinishedWithAssignedUserIdNull() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(FINISHED)
        .assignedUserId(null)
        .build();
    // when & then
    assertThrows(MustInputAssignedUserInfoException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 2L));
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 예약중으로 변경하려는 경우 assignedUserId가 존재하는 경우")
  void changeAdoptionStatus_shouldThrowValidationError_whenChangeToResulvingWithAssignedUserId() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .assignedUserId(1L)
        .build();
    // when & then
    assertThrows(AssignedUserMustBeNullException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 2L));
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 분양중으로 변경하려는 경우 assignedUserId가 존재하는 경우")
  void changeAdoptionStatus_shouldThrowValidationError_whenChangeToAdoptingWithAssignedUserId() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(ADOPTING)
        .assignedUserId(1L)
        .build();
    // when & then
    assertThrows(AssignedUserMustBeNullException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 2L));
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 해당 분양게시글이 존재하지 않을 경우")
  void changeAdoptionStatus_shouldThrowAdoptionNotFound_whenAdoptionIsNotExist() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .build();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());
    //when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 2L));

  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 삭제된 분양 게시글인 경우")
  void changeAdoptionStatus_shouldThrowAdoptionIsDeleted_whenAdoptionIsDeleted() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .build();

    Adoption adoption = spy(Adoption.builder()
        .user(mock(User.class))
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(adoption.getDeletedAt()).willReturn(LocalDateTime.now());
    //when & then
    assertThrows(AdoptionIsDeletedException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 1L));

  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 분양게시글 작성자와 상태 변경 요청자가 다른 경우")
  void changeAdoptionStatus_shouldThrowNoAuthorized_whenRequestUserIsNotAdoptionWriter() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .build();
    Shelter shelter = Shelter.builder()
        .verified(true)
        .user(mock(User.class))
        .build();
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(3L);
    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 1L));

  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 작성자의 보호소가 삭제된 경우")
  void changeAdoptionStatus_throwNoAuthorized_whenShelterIsDeleted() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .build();
    Shelter shelter = spy(Shelter.builder()
        .verified(true)
        .user(mock(User.class))
        .build());
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);
    given(shelter.isDeleted()).willReturn(true);
    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 1L));
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 작성자의 보호소가 존재하지 않는 경우")
  void changeAdoptionStatus_throwNoAuthorized_whenShelterIsNotExist() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .build();
    User user = spy(User.builder().build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);

    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 1L));
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 작성자의 보호소가 인증되지 않은 경우")
  void changeAdoptionStatus_throwNoVerifiedShelterExists_whenShelterIsNotVerified() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .build();
    Shelter shelter = spy(Shelter.builder()
        .verified(false)
        .user(mock(User.class))
        .build());
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);
    given(shelter.isDeleted()).willReturn(false);
    //when & then
    assertThrows(NotVerifiedShelterExistsException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 1L));
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 변경하려는 분양상태가 현재 분양상태와 동일한 경우")
  void changeAdoptionStatus_throwCurrentStatusIsSameToChange_whenCurrentStatusIsSameToChange() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .build();
    Shelter shelter = spy(Shelter.builder()
        .verified(true)
        .user(mock(User.class))
        .build());
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(RESERVING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);
    given(shelter.isDeleted()).willReturn(false);
    //when & then
    assertThrows(CurrentStatusIsSameToChangeException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 1L));
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - AdoptionDocument가 존재하지 않을 경우")
  void changeAdoptionStatus_throwAdoptionIsNotFound_whenAdoptionIsNotExist() {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status(RESERVING)
        .build();
    Shelter shelter = Shelter.builder()
        .verified(true)
        .user(mock(User.class))
        .build();
    User user = spy(User.builder()
        .shelter(shelter)
        .build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(1L);
    given(adoptionRepository.save(any(Adoption.class))).will(returnsFirstArg());
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.empty());
    given(adoption.getId()).willReturn(2L);

    //when & then
    assertThrows(AdoptionDocumentNotFoundException.class,
        () -> adoptionService.changeAdoptionStatus(1L, request, 1L));
  }

  @Test
  @DisplayName("분양게시글 삭제 성공 테스트")
  void deleteAdoption_success() {
    //given
    User user = spy(User.builder().build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());
    AdoptionDocument adoptionDocument = AdoptionDocument.builder().build();

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(3L);
    given(adoption.getId()).willReturn(2L);
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.of(adoptionDocument));

    //when
    adoptionService.deleteAdoption(1L, 3L);
    //then
    ArgumentCaptor<Adoption> adoptionArgumentCaptor = ArgumentCaptor.forClass(
        Adoption.class);
    ArgumentCaptor<AdoptionDocument> adoptionDocumentArgumentCaptor =
        ArgumentCaptor.forClass(AdoptionDocument.class);

    verify(adoptionRepository).save(adoptionArgumentCaptor.capture());
    verify(adoptionSearchRepository).save(adoptionDocumentArgumentCaptor.capture());

    assertNotNull(adoptionArgumentCaptor.getValue().getDeletedAt());
    assertNotNull(adoptionDocumentArgumentCaptor.getValue().getDeletedAt());

  }

  @Test
  @DisplayName("분양 게시글 삭제 - 해당 분양 게시글이 없는 경우")
  void deleteAdoption_shouldThrowAdoptionNotFound_whenAdoptionIsNotExist() {
    //given
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());
    //when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> adoptionService.deleteAdoption(1L, 2L));
  }

  @Test
  @DisplayName("분양 게시글 삭제 - 이미 삭제된 분양 게시글인 경우")
  void deleteAdoption_shouldThrowAdoptionIsDeleted_whenAdoptionIsDeleted() {
    //given
    Adoption adoption = spy(Adoption.builder()
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(adoption.getDeletedAt()).willReturn(LocalDateTime.now());
    //when & then
    assertThrows(AdoptionIsDeletedException.class,
        () -> adoptionService.deleteAdoption(1L, 2L));
  }

  @Test
  @DisplayName("분양 게시글 삭제 - 분양완료된 분양게시글 삭제하는 경우")
  void deleteAdoption_shouldThrowAlreadyFinishedAdoption_whenAdoptionStatusIsFinished() {
    //given
    Adoption adoption = spy(Adoption.builder()
        .status(FINISHED)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    //when & then
    assertThrows(AlreadyFinishedAdoptionException.class,
        () -> adoptionService.deleteAdoption(1L, 2L));
  }

  @Test
  @DisplayName("분양 게시글 삭제 - 분양 게시글 작성자와 삭제 요청자가 다를 경우")
  void deleteAdoption_shouldThrowNoAuthorized_whenRequestUserIsNotAdoptionWriter() {
    //given
    User user = spy(User.builder().build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(6L);
    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.deleteAdoption(1L, 2L));
  }

  @Test
  @DisplayName("분양 게시글 삭제 - ES에 저장된 분양 게시글 정보가 없는 경우")
  void deleteAdoption_shouldThrowAdoptionDocumentNotFound_whenAdoptionDocumentIsNotExist() {
    //given
    User user = spy(User.builder().build());
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .status(ADOPTING)
        .images(List.of(new S3Object("url1"), new S3Object("url2")))
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(user.getId()).willReturn(3L);
    given(adoption.getId()).willReturn(1L);
    given(adoptionSearchRepository.findById(anyLong())).willReturn(Optional.empty());
    //when & then
    assertThrows(AdoptionDocumentNotFoundException.class,
        () -> adoptionService.deleteAdoption(1L, 3L));
  }

  @Test
  @DisplayName("분양 게시글 목록 조회 성공 테스트")
  void getAdoptionList_success() {
    //given
    AdoptionSearchRequest request = null;
    PageRequest pageRequest = PageRequest.of(0, 10,
        Sort.by(Direction.fromString("desc"), "createdAt"));

    given(queryRepository.findByAdoptionSearchRequest(request, pageRequest))
        .willReturn(createAdoptionDocumentList(4));
    //when
    Slice<AdoptionSearchResponse> responses = adoptionService.getAdoptionList(request,
        pageRequest);
    //then
    assertEquals(4, responses.getSize());
    assertEquals(1, responses.getContent().get(0).getAdoptionId());
    assertEquals(2, responses.getContent().get(1).getAdoptionId());
    assertEquals(3, responses.getContent().get(2).getAdoptionId());
    assertEquals(4, responses.getContent().get(3).getAdoptionId());
  }

  private List<AdoptionDocument> createAdoptionDocumentList(int addNum) {
    List<AdoptionDocument> responses = new ArrayList<>();
    for (int i = 1; i <= addNum; i++) {
      responses.add(AdoptionDocument.builder()
          .id(Integer.toUnsignedLong(i))
          .user(mock(UserDocumentVo.class))
          .build());
    }
    return responses;
  }

  private List<MultipartFile> createImageList(int addSize) throws IOException {
    List<MultipartFile> imageList = new ArrayList<>();
    for (int i = 1; i <= addSize; i++) {
      MockMultipartFile image = MockDataUtil.createMockMultipartFile("images",
          "src/test/resources/images/banzzokee" + i + ".png");
      imageList.add(image);
    }
    return imageList;
  }

  private List<FileDto> createFileDtoList(int addSize) {
    List<FileDto> fileDtoList = new ArrayList<>();
    for (int i = 1; i <= addSize; i++) {
      FileDto fileDto = new FileDto("url" + i, "filename" + i + ".png");
      fileDtoList.add(fileDto);
    }
    return fileDtoList;
  }
}