package homes.banzzokee.domain.adoption.service;

import static homes.banzzokee.domain.type.AdoptionStatus.ADOPTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.dto.AdoptionRegisterRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionResponse;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionIsDeletedException;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.shelter.exception.NotVerifiedShelterExistsException;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.infra.fileupload.dto.FileDto;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import java.io.IOException;
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
  @InjectMocks
  private AdoptionService adoptionService;

  private final AdoptionRegisterRequest request = AdoptionRegisterRequest.builder()
      .title("강아지")
      .content("귀여운 강아지를 소개합니다.")
      .breed("포메라니안")
      .size("중형")
      .neutering(false)
      .gender("수컷")
      .age(10)
      .healthChecked(true)
      .registeredAt("2024-01-01")
      .build();

  private final List<MultipartFile> images = createImageList(4);

  AdoptionServiceTest() throws IOException {
  }

  @Test
  @DisplayName("분양게시글 등록 성공 테스트")
  void registerAdoption_success() throws IOException {
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
    adoptionService.registerAdoption(request, images, 1L);

    //then
    ArgumentCaptor<AdoptionDocument> adoptionDocumentCaptor = ArgumentCaptor.forClass(
        AdoptionDocument.class);
    verify(adoptionSearchRepository).save(adoptionDocumentCaptor.capture());

    // ES 저장되는 AdoptionDocument 객체 검증
    assertEquals(user.getEmail(), adoptionDocumentCaptor.getValue().getUser().getEmail());
    assertEquals(user.getShelter().getDescription(),
        adoptionDocumentCaptor.getValue().getUser().getShelter().getDescription());
    assertEquals(request.getTitle(), adoptionDocumentCaptor.getValue().getTitle());
    assertEquals(request.getContent(), adoptionDocumentCaptor.getValue().getContent());
    assertEquals(request.getBreed(), adoptionDocumentCaptor.getValue().getBreed());
    assertEquals(request.getSize(), adoptionDocumentCaptor.getValue().getSize());
    assertEquals(request.isNeutering(), adoptionDocumentCaptor.getValue().isNeutering());
    assertEquals(request.getGender(), adoptionDocumentCaptor.getValue().getGender());
    assertEquals(request.getAge(), adoptionDocumentCaptor.getValue().getAge());
    assertEquals(request.isHealthChecked(),
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
    assertEquals(ADOPTING.getStatus(), adoptionDocumentCaptor.getValue().getStatus());
  }

  @Test
  @DisplayName("user가 존재하지 않으면 UserNotFoundException 발생")
  void registerAdoption_shouldThrowUserNotFoundException_whenUserIsNotExist() {
    //given
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());
    //when & then
    assertThrows(UserNotFoundException.class,
        () -> adoptionService.registerAdoption(request, images, 1L));
  }

  @Test
  @DisplayName("user에 등록된 shelter가 없으면 NoAuthorizedException")
  void registerAdoption_shouldThrowNoAuthorizedException_whenUserIsNotRegisteredShelter() {
    //given
    User user = User.builder()
        .email("abcd@abcd.com")
        .build();

    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> adoptionService.registerAdoption(request, images, 1L));
  }

  @Test
  @DisplayName("user에 등록된 shelter가 삭제된 경우 NoAuthorizedException")
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
        () -> adoptionService.registerAdoption(request, images, 1L));
  }

  @Test
  @DisplayName("인증된 shelter가 아닌 경우 NotVerifiedShelterExistsException 발생")
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
        () -> adoptionService.registerAdoption(request, images, 1L));
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
    assertEquals("포메라니안", response.getBreed());
    assertEquals("분양중", response.getStatus());
    assertEquals(now.toLocalDate(), response.getUser().getJoinedAt());
  }

  @Test
  @DisplayName("분양게시글 존재하지 않을 경우 예외 처리")
  void getAdoption_shouldThrowAdoptionNotFoundException_whenAdoptionIsNotExist() {
    //given
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());
    //when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> adoptionService.getAdoption(2L));
  }

  @Test
  @DisplayName("분양게시글 삭제된 경우 예외 처리")
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

  private List<MultipartFile> createImageList(int addSize) throws IOException {
    List<MultipartFile> imageList = new ArrayList<>();
    for (int i = 1; i <= addSize; i++) {
      MockMultipartFile image = MockDataUtil.createMockMultipartFile("images",
          "src/test/resources/images/banzzokee" + i + ".png");
      imageList.add(image);
    }
    return imageList;
  }

  private List<FileDto> createFileDtoList(int addSize) throws IOException {
    List<FileDto> fileDtoList = new ArrayList<>();
    for (int i = 1; i <= addSize; i++) {
      FileDto fileDto = new FileDto("url" + i, "filename" + i + ".png");
      fileDtoList.add(fileDto);
    }
    return fileDtoList;
  }
}