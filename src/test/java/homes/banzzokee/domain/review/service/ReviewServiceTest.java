package homes.banzzokee.domain.review.service;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionDocumentNotFoundException;
import homes.banzzokee.domain.adoption.exception.AdoptionIsDeletedException;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.review.dao.ReviewRepository;
import homes.banzzokee.domain.review.dto.ReviewRegisterRequest;
import homes.banzzokee.domain.review.elasticsearch.dao.ReviewDocumentRepository;
import homes.banzzokee.domain.review.elasticsearch.document.ReviewDocument;
import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.review.exception.OneReviewPerAdoptionException;
import homes.banzzokee.domain.review.exception.ReviewPermissionException;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.infra.fileupload.dto.FileDto;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import java.io.IOException;
import java.time.LocalDate;
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
class ReviewServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private AdoptionRepository adoptionRepository;
  @Mock
  private FileUploadService fileUploadService;
  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private ReviewDocumentRepository reviewDocumentRepository;
  @Mock
  private AdoptionSearchRepository adoptionSearchRepository;
  @InjectMocks
  private ReviewService reviewService;

  private final ReviewRegisterRequest registerRequest = ReviewRegisterRequest.builder()
      .adoptionId(1L)
      .title("강아지 입양")
      .content("너무 귀여워요")
      .build();

  private final List<MultipartFile> images = createImageList(4);

  ReviewServiceTest() throws IOException {
  }

  @Test
  @DisplayName("후기 게시글 등록 성공")
  void registerReview_success() {
    //given
    User user = mock(User.class);
    Adoption adoption = Adoption.builder().user(user).build();
    LocalDate now = LocalDate.now();
    adoption.updateStatusToFinish(AdoptionStatus.FINISHED, user, now);
    AdoptionDocument adoptionDocument = AdoptionDocument.builder().build();

    List<FileDto> fileDtoList = createFileDtoList(4);

    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(fileUploadService.uploadManyFile(images, FilePath.REVIEW)).willReturn(
        fileDtoList);
    given(reviewRepository.save(any(Review.class))).will(returnsFirstArg());
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.of(adoptionDocument));
    given(user.getId()).willReturn(1L);
    given(user.getNickname()).willReturn("방울이");
    given(user.getCreatedAt()).willReturn(now.atStartOfDay());

    //when
    reviewService.registerReview(registerRequest, images, 1L);

    //then
    ArgumentCaptor<AdoptionDocument> adoptionDocumentCaptor = ArgumentCaptor.forClass(
        AdoptionDocument.class);
    ArgumentCaptor<ReviewDocument> reviewDocumentCaptor = ArgumentCaptor.forClass(
        ReviewDocument.class);

    verify(adoptionSearchRepository).save(adoptionDocumentCaptor.capture());
    verify(reviewDocumentRepository).save(reviewDocumentCaptor.capture());

    assertEquals("강아지 입양", adoptionDocumentCaptor.getValue().getReview().getTitle());
    assertEquals("너무 귀여워요", adoptionDocumentCaptor.getValue().getReview().getContent());
    assertEquals(fileDtoList.size(),
        adoptionDocumentCaptor.getValue().getReview().getImages().size());
    for (int i = 0; i < 4; i++) {
      assertEquals(fileDtoList.get(i).getUrl(),
          adoptionDocumentCaptor.getValue().getReview().getImages().get(i));
    }

    assertEquals(1L, reviewDocumentCaptor.getValue().getAdoption().getUserId());
    assertEquals("방울이", reviewDocumentCaptor.getValue().getAdoption().getUserNickname());
    assertEquals("분양완료", reviewDocumentCaptor.getValue().getAdoption().getStatus());
    assertEquals(now, reviewDocumentCaptor.getValue().getAdoption().getAdoptedAt());
    assertEquals(now, reviewDocumentCaptor.getValue().getUser().getJoinedAt());
    assertEquals(1L, reviewDocumentCaptor.getValue().getUser().getUserId());
    assertEquals("방울이", reviewDocumentCaptor.getValue().getUser().getNickname());
    assertEquals("강아지 입양", reviewDocumentCaptor.getValue().getTitle());
    assertEquals("너무 귀여워요", reviewDocumentCaptor.getValue().getContent());
    for (int i = 0; i < 4; i++) {
      assertEquals(fileDtoList.get(i).getUrl(),
          reviewDocumentCaptor.getValue().getImages().get(i).getUrl());
    }
  }

  @Test
  @DisplayName("후기게시글 등록 - 등록 요청하려는 사용자가 존재하지 않을 경우")
  void registerReview_shouldThrowUserNotFoundException_whenUserIsNotExist() {
    //given
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    //when & then
    assertThrows(UserNotFoundException.class,
        () -> reviewService.registerReview(registerRequest, images, 1L));

  }

  @Test
  @DisplayName("후기 게시글 등록 - 분양게시글이 존재하지 않을 경우")
  void registerReview_shouldThrowAdoptionNotFoundException_whenAdoptionIsNotExist() {
    //given
    User user = mock(User.class);

    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());

    //when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> reviewService.registerReview(registerRequest, images, 1L));
  }

  @Test
  @DisplayName("후기 게시글 등록 - 분양게시글이 삭제된 경우")
  void registerReview_shouldThrowAdoptionNotFoundException_whenAdoptionIsDeleted() {
    //given
    User user = mock(User.class);
    Adoption adoption = spy(Adoption.builder().user(user).build());

    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());
    given(adoption.isDeleted()).willReturn(true);

    //when & then
    assertThrows(AdoptionIsDeletedException.class,
        () -> reviewService.registerReview(registerRequest, images, 1L));
  }

  @Test
  @DisplayName("후기 게시글 등록 - 분양 게시글의 입양자와 후기 게시글 등록하려는 사용자가 다를 경우")
  void registerReview_shouldThrowReviewPermissionException_whenUserIsNotAssignedUser() {
    //given
    User requestUser = mock(User.class);
    User assignedUser = mock(User.class);
    Adoption adoption = Adoption.builder().user(requestUser).build();
    LocalDate now = LocalDate.now();
    adoption.updateStatusToFinish(AdoptionStatus.FINISHED, assignedUser, now);

    given(userRepository.findById(anyLong())).willReturn(Optional.of(requestUser));
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    //when & then
    assertThrows(ReviewPermissionException.class,
        () -> reviewService.registerReview(registerRequest, images, 1L));
  }

  @Test
  @DisplayName("후기 게시글 등록 - 분양게시글의 후기게시글이 이미 존재하는 경우")
  void registerReview_shouldThrowOneReviewPerAdoptionException_whenReviewIsAlreadyExist() {
    //given
    User user = mock(User.class);
    Review review = mock(Review.class);
    Adoption adoption = spy(Adoption.builder().user(user).build());
    LocalDate now = LocalDate.now();
    adoption.updateStatusToFinish(AdoptionStatus.FINISHED, user, now);

    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(adoption.getReview()).willReturn(review);
    given(review.isDeleted()).willReturn(false);

    //when & then
    assertThrows(OneReviewPerAdoptionException.class,
        () -> reviewService.registerReview(registerRequest, images, 1L));
  }

  @Test
  @DisplayName("후기 게시글 등록 - ES에 저장된 분양게시글 document가 없는 경우")
  void registerReview_shouldThrowAdoptionDocumentNotFoundException_whenDocumentIsNotExist() {
    //given
    User user = mock(User.class);
    Adoption adoption = Adoption.builder().user(user).build();
    LocalDate now = LocalDate.now();
    adoption.updateStatusToFinish(AdoptionStatus.FINISHED, user, now);

    List<FileDto> fileDtoList = createFileDtoList(4);

    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
    given(fileUploadService.uploadManyFile(images, FilePath.REVIEW)).willReturn(
        fileDtoList);
    given(reviewRepository.save(any(Review.class))).will(returnsFirstArg());
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.empty());

    //when & then
    assertThrows(AdoptionDocumentNotFoundException.class,
        () -> reviewService.registerReview(registerRequest, images, 1L));
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