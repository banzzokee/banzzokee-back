package homes.banzzokee.domain.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.review.dto.ReviewRegisterRequest;
import homes.banzzokee.domain.review.service.ReviewService;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import homes.banzzokee.global.util.MockDataUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(value = ReviewController.class, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)})
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ReviewService reviewService;

  @Captor
  private ArgumentCaptor<List<MultipartFile>> imagesCaptor;

  private final ReviewRegisterRequest registerRequest = ReviewRegisterRequest.builder()
      .adoptionId(1L)
      .title("강아지 입양")
      .content("너무 귀여워요")
      .build();

  @BeforeEach
  void setup() {
    User user = mock(User.class);
    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    when(user.getId()).thenReturn(1L);
    when(userDetails.getUserId()).thenReturn(1L);
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(userDetails, "", null));
  }

  @Test
  @DisplayName("리뷰 등록 성공 테스트")
  void registerReview_success() throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", registerRequest);

    //when
    MockMultipartHttpServletRequestBuilder postWithoutImage = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/reviews")
        .part(mockPart);
    MockMultipartHttpServletRequestBuilder post = addImages(postWithoutImage, 4);
    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isOk());

    ArgumentCaptor<ReviewRegisterRequest> requestArgumentCaptor = ArgumentCaptor.forClass(
        ReviewRegisterRequest.class);
    ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

    verify(reviewService).registerReview(requestArgumentCaptor.capture(),
        imagesCaptor.capture(), userIdCaptor.capture());

    assertEquals(registerRequest, requestArgumentCaptor.getValue());
    assertEquals(4, imagesCaptor.getValue().size());
    assertEquals("banzzokee1.png", imagesCaptor.getValue().get(0).getOriginalFilename());
    assertEquals("banzzokee2.png", imagesCaptor.getValue().get(1).getOriginalFilename());
    assertEquals("banzzokee3.png", imagesCaptor.getValue().get(2).getOriginalFilename());
    assertEquals("banzzokee4.png", imagesCaptor.getValue().get(3).getOriginalFilename());
    assertEquals(1, userIdCaptor.getValue());
  }

  @Test
  @DisplayName("후기 게시글 등록 - 같은 파일명인 여러 파일을 업로드 할 경우")
  void registerReview_shouldThrowValidationError_whenFileHasDuplicateFilename()
      throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", registerRequest);

    //when
    MockMultipartFile image = MockDataUtil.createMockMultipartFile("images",
        "src/test/resources/images/banzzokee.png");
    MockMultipartHttpServletRequestBuilder post = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/reviews")
        .part(mockPart)
        .file(image)
        .file(image);

    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));
  }

  @Test
  @DisplayName("후기 게시글 등록 - 이미지 파일 8장 초과일 경우")
  void registerReview_shouldThrowValidationError_whenNumberOfFilesOver8()
      throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", registerRequest);

    //when
    MockMultipartHttpServletRequestBuilder postWithoutImage = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/reviews")
        .part(mockPart);
    MockMultipartHttpServletRequestBuilder post = addImages(postWithoutImage, 9);
    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));

  }

  @Test
  @DisplayName("후기 게시글 등록 - 이미지 파일이 아닐 경우 예외 처")
  void registerReview_shouldThrowValidationError_whenFileIsNotImage()
      throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", registerRequest);

    //when
    MockMultipartFile textFile = createMockTextFile();
    MockMultipartHttpServletRequestBuilder post = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/reviews")
        .part(mockPart)
        .file(textFile);

    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));

  }

  @Test
  @DisplayName("후기 게시글 등록 - request 필드 유효성 검사 실패")
  void registerReview_shouldThrowValidationError_whenInputInvalidRequest()
      throws Exception {
    String over50 = "over".repeat(50);
    String over500 = "over".repeat(500);
    ReviewRegisterRequest request = ReviewRegisterRequest.builder()
        .adoptionId(1L)
        .title(over50)  // 50자 이상
        .content(over500)  // 500자 이상
        .build();
    MockPart mockPart = MockDataUtil.createMockPart("request", request);

    //when
    MockMultipartHttpServletRequestBuilder postWithoutImage = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/reviews")
        .part(mockPart);
    MockMultipartHttpServletRequestBuilder post = addImages(postWithoutImage, 2);
    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));
  }

  private MockMultipartHttpServletRequestBuilder addImages(
      MockMultipartHttpServletRequestBuilder postWithoutImage, int addSize)
      throws IOException {

    for (int i = 1; i <= addSize; i++) {
      MockMultipartFile image = MockDataUtil.createMockMultipartFile("images",
          "src/test/resources/images/banzzokee" + i + ".png");
      postWithoutImage.file(image);
    }
    return postWithoutImage;
  }

  private MockMultipartFile createMockTextFile() throws IOException {
    // 임시 파일 생성
    Path tempFile = Files.createTempFile("testFile", ".txt");
    Files.write(tempFile, "Upload Test".getBytes());

    // MultipartFile 로 변환
    String originalFilename = "testFile.txt";
    String name = "images";
    String contentType = "text/plain";
    byte[] content = Files.readAllBytes(tempFile);

    return new MockMultipartFile(name, originalFilename, contentType, content);
  }

}