package homes.banzzokee.domain.shelter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.shelter.dto.ShelterRegisterRequest;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.shelter.exception.UserAlreadyRegisterShelterException;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.infra.fileupload.dto.ImageDto;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import java.io.IOException;
import java.util.Optional;
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
  private FileUploadService s3Service;

  @InjectMocks
  private ShelterService shelterService;

  private final User mockUser = mock(User.class);
  private final MultipartFile mockFile = MockDataUtil.createMockMultipartFile(
      "src/test/resources/images/banzzokee.png");

  private final ShelterRegisterRequest mockShelterRegisterRequest
      = mock(ShelterRegisterRequest.class);

  ShelterServiceTest() throws IOException {
  }

  @BeforeEach
  public void setup() {
    given(mockUser.getId()).willReturn(1L);
  }


  @Test
  @DisplayName("[보호소 등록] - 사용자를 못찾으면 UserNotFoundException 발생")
  void registerShelter_when_userNotExists_then_throwUserNotFoundException() {
    // given
    given(userRepository.findById(mockUser.getId()))
        .willThrow(new UserNotFoundException());

    // when
    // then
    assertThrows(UserNotFoundException.class,
        () -> shelterService.registerShelter(mockShelterRegisterRequest,
            mockFile,
            mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 등록] - 보호소 권한이 있으면 UserAlreadyRegisterShelterException 발생")
  void registerShelter_when_hasShelterRole_then_throwUserAlreadyRegisterShelterException() {
    // given
    given(mockUser.hasShelter()).willReturn(true);
    given(userRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));

    // when
    // then
    assertThrows(UserAlreadyRegisterShelterException.class,
        () -> shelterService.registerShelter(mockShelterRegisterRequest,
            mockFile,
            mockUser.getId()));
  }

  @Test
  @DisplayName("[보호소 등록] - 성공 검증")
  void registerShelter_success_verify() {
    // given
    given(mockUser.hasShelter()).willReturn(false);
    given(userRepository.findById(mockUser.getId()))
        .willReturn(Optional.of(mockUser));

    ImageDto image = ImageDto.builder()
        .url("url")
        .filename("filename")
        .build();
    given(s3Service.uploadOneFile(any(MultipartFile.class)))
        .willReturn(image);

    // when
    ShelterRegisterRequest request = ShelterRegisterRequest.builder()
        .name("보호소")
        .description("설명")
        .tel("02-1234-5678")
        .address("주소")
        .latitude(24.0)
        .longitude(37.0)
        .build();

    shelterService.registerShelter(request, mockFile, mockUser.getId());

    // then
    // 이미지 업로드 검증
    ArgumentCaptor<MultipartFile> shelterImgCaptor
        = ArgumentCaptor.forClass(MultipartFile.class);
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
    assertEquals(image.getUrl(), shelter.getShelterImgUrl());
    assertFalse(shelter.isVerified());
  }
}