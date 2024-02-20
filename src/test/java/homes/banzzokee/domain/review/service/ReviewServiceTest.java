package homes.banzzokee.domain.review.service;

import static homes.banzzokee.domain.type.AdoptionStatus.FINISHED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchQueryRepository;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.elasticsearch.document.subclass.ReviewDocumentVo;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionDocumentNotFoundException;
import homes.banzzokee.domain.adoption.exception.AdoptionIsDeletedException;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.review.dao.ReviewRepository;
import homes.banzzokee.domain.review.dto.ReviewRegisterRequest;
import homes.banzzokee.domain.review.dto.ReviewResponse;
import homes.banzzokee.domain.review.dto.ReviewSearchResponse;
import homes.banzzokee.domain.review.dto.ReviewUpdateRequest;
import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.review.exception.DeletedReviewException;
import homes.banzzokee.domain.review.exception.OneReviewPerAdoptionException;
import homes.banzzokee.domain.review.exception.ReviewNotFoundException;
import homes.banzzokee.domain.review.exception.ReviewPermissionException;
import homes.banzzokee.domain.type.AdoptionStatus;
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
  private AdoptionSearchRepository adoptionSearchRepository;
  @Mock
  private AdoptionSearchQueryRepository adoptionSearchQueryRepository;
  @InjectMocks
  private ReviewService reviewService;

  private final ReviewRegisterRequest registerRequest = ReviewRegisterRequest.builder()
      .adoptionId(1L)
      .title("강아지 입양")
      .content("너무 귀여워요")
      .build();

  private final ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
      .title("강아지 커여워")
      .content("후기 게시글 내용 수정")
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
    adoption.updateStatusToFinish(AdoptionStatus.FINISHED, user);
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

    //when
    reviewService.registerReview(registerRequest, images, 1L);

    //then
    ArgumentCaptor<AdoptionDocument> adoptionDocumentCaptor = ArgumentCaptor.forClass(
        AdoptionDocument.class);

    verify(adoptionSearchRepository).save(adoptionDocumentCaptor.capture());

    assertEquals("강아지 입양", adoptionDocumentCaptor.getValue().getReview().getTitle());
    assertEquals("너무 귀여워요", adoptionDocumentCaptor.getValue().getReview().getContent());
    assertEquals(fileDtoList.size(),
        adoptionDocumentCaptor.getValue().getReview().getImages().size());
    for (int i = 0; i < 4; i++) {
      assertEquals(fileDtoList.get(i).getUrl(),
          adoptionDocumentCaptor.getValue().getReview().getImages().get(i).getUrl());
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
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));
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
    adoption.updateStatusToFinish(AdoptionStatus.FINISHED, assignedUser);

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
    adoption.updateStatusToFinish(AdoptionStatus.FINISHED, user);

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
    adoption.updateStatusToFinish(AdoptionStatus.FINISHED, user);

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

  @Test
  @DisplayName("후기 게시글 상세정보 조회 성공 테스트")
  void getReview_success() {
    //given
    User adoptionUser = User.builder()
        .nickname("분양 게시글 작성자")
        .build();
    User assignedUser = spy(User.builder()
        .nickname("입양자")
        .build());
    Adoption adoption = Adoption.builder()
        .title("강아지")
        .user(adoptionUser)
        .breed(BreedType.findByString("포메라니안"))
        .status(FINISHED)
        .build();
    Review review = Review.builder()
        .title("후기 게시글")
        .adoption(adoption)
        .user(assignedUser)
        .build();

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(assignedUser.getCreatedAt()).willReturn(LocalDate.now().atStartOfDay());
    //when
    ReviewResponse response = reviewService.getReview(1L);

    //then
    assertEquals("분양 게시글 작성자", response.getAdoption().getUserNickname());
    assertEquals("입양자", response.getUser().getNickname());
    assertEquals("강아지", response.getAdoption().getTitle());
    assertEquals("포메라니안", response.getAdoption().getBreed().getValue());
    assertEquals("분양완료", response.getAdoption().getStatus().getValue());
    assertEquals("후기 게시글", response.getTitle());

  }

  @Test
  @DisplayName("후기 게시글 상세정보 조회 - 후기 게시글 존재하지 않을 경우")
  void getReview_shouldThrowReviewNotFoundException_whenReviewIsNotExist() {
    //given
    given(reviewRepository.findById(anyLong())).willReturn(Optional.empty());

    //when & then
    assertThrows(ReviewNotFoundException.class, () -> reviewService.getReview(1L));

  }

  @Test
  @DisplayName("후기 게시글 상세정보 조회 - 후기 게시글 삭제된 않을 경우")
  void getReview_shouldThrowDeletedReviewException_whenReviewIsDeleted() {
    //given
    Review review = spy(Review.builder()
        .title("후기 게시글")
        .build());

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(review.isDeleted()).willReturn(true);

    //when & then
    assertThrows(DeletedReviewException.class, () -> reviewService.getReview(1L));

  }

  @Test
  @DisplayName("후기 게시글 수정 성공 테스트")
  void updateReview_success() {
    //given
    User assignedUser = spy(User.builder().build());
    User user = mock(User.class);
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .build());
    Review review = spy(Review.builder()
        .user(assignedUser)
        .adoption(adoption)
        .build());
    List<FileDto> fileDtoList = createFileDtoList(4);
    AdoptionDocument adoptionDocument = AdoptionDocument.builder().build();

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(assignedUser));
    given(fileUploadService.uploadManyFile(images, FilePath.REVIEW)).willReturn(
        fileDtoList);
    given(reviewRepository.save(any(Review.class))).will(returnsFirstArg());
    given(adoption.getId()).willReturn(1L);
    given(user.getId()).willReturn(1L);
    given(assignedUser.getCreatedAt()).willReturn(LocalDateTime.now());
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.of(adoptionDocument));
    given(review.getId()).willReturn(1L);
    given(assignedUser.getId()).willReturn(1L);
    given(assignedUser.getNickname()).willReturn("Happy");

    //when
    ReviewResponse response = reviewService.updateReview(1L, updateRequest, images, 1L);

    //then
    assertEquals(updateRequest.getTitle(), response.getTitle());
    assertEquals(updateRequest.getContent(), response.getContent());
    assertEquals(4, response.getImages().size());
    assertEquals("url1", response.getImages().get(0));
    assertEquals("url2", response.getImages().get(1));
    assertEquals("url3", response.getImages().get(2));
    assertEquals("url4", response.getImages().get(3));

  }

  @Test
  @DisplayName("후기게시글 수정 - 수정하려는 후기 게시글이 존재하지 않을 경우")
  void updateReview_shouldThrowReviewNotFoundException_whenReviewIsNotExist() {
    //given
    given(reviewRepository.findById(anyLong())).willReturn(Optional.empty());

    //when & then
    assertThrows(ReviewNotFoundException.class,
        () -> reviewService.updateReview(1L, updateRequest, images, 1L));
  }

  @Test
  @DisplayName("후기게시글 수정 - 수정하려는 사용자가 db에 존재하지 않을 경우")
  void updateReview_shouldThrowUserNotFoundException_whenUserIsNotExist() {
    //given
    Review review = mock(Review.class);

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    //when & then
    assertThrows(UserNotFoundException.class,
        () -> reviewService.updateReview(1L, updateRequest, images, 1L));
  }

  @Test
  @DisplayName("후기게시글 수정 - 수정하려는 후기 게시글이 이미 삭제된 경우")
  void updateReview_shouldThrowDeletedReviewException_whenReviewIsDeleted() {
    //given
    User assignedUser = spy(User.builder().build());
    User user = mock(User.class);
    Review review = spy(Review.builder()
        .user(assignedUser)
        .build());

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(review.isDeleted()).willReturn(true);

    //when & then
    assertThrows(DeletedReviewException.class,
        () -> reviewService.updateReview(1L, updateRequest, images, 1L));
  }

  @Test
  @DisplayName("후기게시글 수정 - 후기 게시글 작성자와 수정 요청자가 다른 경우")
  void updateReview_shouldThrowNoAuthorizedException_whenUserIsNotReviewWriter() {
    //given
    User assignedUser = spy(User.builder().build());
    User user = mock(User.class);
    Review review = spy(Review.builder()
        .user(assignedUser)
        .build());

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> reviewService.updateReview(1L, updateRequest, images, 1L));

  }

  @Test
  @DisplayName("후기게시글 수정 - ES에 저장된 분양 게시글 정보가 존재하지 않을 경")
  void updateReview_shouldThrowAdoptionDocumentNotFoundException_whenAdoptionDocumentIsNotExist() {
    //given
    User assignedUser = spy(User.builder().build());
    User user = mock(User.class);
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .build());
    Review review = spy(Review.builder()
        .user(assignedUser)
        .adoption(adoption)
        .build());
    List<FileDto> fileDtoList = createFileDtoList(4);

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(assignedUser));
    given(fileUploadService.uploadManyFile(images, FilePath.REVIEW)).willReturn(
        fileDtoList);
    given(reviewRepository.save(any(Review.class))).will(returnsFirstArg());
    given(adoption.getId()).willReturn(1L);
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.empty());

    //when & then
    assertThrows(AdoptionDocumentNotFoundException.class,
        () -> reviewService.updateReview(1L, updateRequest, images, 1L));

  }

  @Test
  @DisplayName("후기 게시글 삭제 성공 테스트")
  void deleteReview_success() {
    // given
    User assignedUser = spy(User.builder().build());
    User user = mock(User.class);
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .build());
    Review review = spy(Review.builder()
        .user(assignedUser)
        .adoption(adoption)
        .build());
    adoption.updateReview(review);
    AdoptionDocument adoptionDocument = AdoptionDocument.builder()
        .review(mock(ReviewDocumentVo.class))
        .build();

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(assignedUser));
    given(adoption.getId()).willReturn(1L);
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.of(adoptionDocument));

    // when
    reviewService.deleteReview(1L, 1L);

    // then
    ArgumentCaptor<AdoptionDocument> adoptionDocumentArgumentCaptor =
        ArgumentCaptor.forClass(AdoptionDocument.class);
    ArgumentCaptor<Review> reviewArgumentCaptor = ArgumentCaptor.forClass(Review.class);

    verify(adoptionSearchRepository).save(adoptionDocumentArgumentCaptor.capture());
    verify(reviewRepository).save(reviewArgumentCaptor.capture());

    assertNull(adoptionDocumentArgumentCaptor.getValue().getReview());
    assertNotNull(reviewArgumentCaptor.getValue().getDeletedAt());
    assertTrue(reviewArgumentCaptor.getValue().getAdoption().getReview().isDeleted());
  }

  @Test
  @DisplayName("후기 게시글 삭제 - 삭제할 후기 게시글이 존재하지 않는 경우")
  void deleteReview_shouldThrowReviewNotFoundException_whenReviewIsNotExist() {
    // given
    given(reviewRepository.findById(anyLong())).willReturn(Optional.empty());

    //when & then
    assertThrows(ReviewNotFoundException.class,
        () -> reviewService.deleteReview(1L, 1L));
  }

  @Test
  @DisplayName("후기 게시글 삭제 - 삭제 요청하는 사용자가 존재하지 않는 경우")
  void deleteReview_shouldThrowUserNotFoundException_whenUserIsNotExist() {
    // given
    Review review = mock(Review.class);

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    //when & then
    assertThrows(UserNotFoundException.class,
        () -> reviewService.deleteReview(1L, 1L));
  }

  @Test
  @DisplayName("후기 게시글 삭제 - 이미 삭제된 후기게시글인 경우")
  void deleteReview_shouldThrowDeletedReviewException_whenReviewIsDeleted() {
    // given
    Review review = mock(Review.class);
    User user = mock(User.class);

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(review.isDeleted()).willReturn(true);

    //when & then
    assertThrows(DeletedReviewException.class,
        () -> reviewService.deleteReview(1L, 1L));
  }

  @Test
  @DisplayName("후기 게시글 삭제 - 후기게시글 작성자와 삭제 요청 사용자가 다른 경우")
  void deleteReview_shouldThrowNoAuthorizedException_whenUserIsNotWriter() {
    // given
    User assignedUser = spy(User.builder().build());
    User user = mock(User.class);
    Review review = spy(Review.builder()
        .user(assignedUser)
        .build());

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

    //when & then
    assertThrows(NoAuthorizedException.class,
        () -> reviewService.deleteReview(1L, 1L));
  }

  @Test
  @DisplayName("후기 게시글 삭제 - ES에 저장된 분양게시글 정보가 없는 경우")
  void deleteReview_shouldThrowAdoptionDocumentNotFoundException_whenAdoptionDocumentIsNotExist() {
    // given
    User assignedUser = spy(User.builder().build());
    User user = mock(User.class);
    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .build());
    Review review = spy(Review.builder()
        .user(assignedUser)
        .adoption(adoption)
        .build());

    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(assignedUser));
    given(adoption.getId()).willReturn(1L);
    given(adoptionSearchRepository.findById(anyLong())).willReturn(
        Optional.empty());

    //when & then
    assertThrows(AdoptionDocumentNotFoundException.class,
        () -> reviewService.deleteReview(1L, 1L));
  }

  @Test
  @DisplayName("후기게시글 목록 조회 성공 테스트")
  void getReviewList_success() {
    //given
    AdoptionDocument adoptionDocument1 = AdoptionDocument.builder()
        .review(ReviewDocumentVo.builder()
            .reviewId(1L)
            .title("후기게시글")
            .build())
        .build();
    AdoptionDocument adoptionDocument2 = AdoptionDocument.builder()
        .review(ReviewDocumentVo.builder()
            .reviewId(2L)
            .title("후기게시글")
            .build())
        .build();
    List<AdoptionDocument> reviewDocuments = List.of(adoptionDocument1, adoptionDocument2);
    PageRequest pageRequest = PageRequest.of(0, 10,
        Sort.by(Direction.fromString("desc"), "createdAt"));

    given(adoptionSearchQueryRepository.findAllReview(pageRequest)).willReturn(
        reviewDocuments);

    //when
    Slice<ReviewSearchResponse> reviewList = reviewService.getReviewList(pageRequest);

    //then
    assertEquals(2, reviewList.getSize());
    assertEquals(1, reviewList.getContent().get(0).getReviewId());
    assertEquals(2, reviewList.getContent().get(1).getReviewId());
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