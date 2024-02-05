package homes.banzzokee.domain.adoption.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.adoption.dto.AdoptionRegisterRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionResponse;
import homes.banzzokee.domain.adoption.dto.AdoptionStatusChangeRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionUpdateRequest;
import homes.banzzokee.domain.adoption.service.AdoptionService;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.global.util.MockMvcUtil;
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
import org.springframework.http.MediaType;
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
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(value = AdoptionController.class, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)})
@AutoConfigureMockMvc(addFilters = false)
class AdoptionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AdoptionService adoptionService;

  @Captor
  private ArgumentCaptor<List<MultipartFile>> imagesCaptor;

  private ObjectMapper objectMapper = new ObjectMapper();

  private final AdoptionRegisterRequest registerRequest = AdoptionRegisterRequest.builder()
      .title("이쁜 우리 강아지")
      .content("우리 강아지를 소개합니다.")
      .breed("포메라니안")
      .size("중형")
      .neutering(false)
      .gender("수컷")
      .age(5)
      .healthChecked(true)
      .registeredAt("2024-01-01")
      .build();

  private final AdoptionUpdateRequest updateRequest = AdoptionUpdateRequest.builder()
      .title("이쁜 우리 강아지")
      .content("우리 강아지를 소개합니다.")
      .breed("포메라니안")
      .size("중형")
      .neutering(false)
      .gender("수컷")
      .age(5)
      .healthChecked(true)
      .registeredAt("2024-01-01")
      .build();

  @BeforeEach
  void setup() {
    User user = mock(User.class);
    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    when(userDetails.getUser()).thenReturn(user);
    when(user.getId()).thenReturn(1L);
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(userDetails, "", null));
  }

  @Test
  @DisplayName("분양게시글 등록 성공 테스트")
  void registerAdoption_success() throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", registerRequest);

    //when
    MockMultipartHttpServletRequestBuilder postWithoutImage = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/adoptions")
        .part(mockPart);
    MockMultipartHttpServletRequestBuilder post = addImages(postWithoutImage, 4);
    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isOk());

    ArgumentCaptor<AdoptionRegisterRequest> requestArgumentCaptor = ArgumentCaptor.forClass(
        AdoptionRegisterRequest.class);
    ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

    verify(adoptionService).registerAdoption(requestArgumentCaptor.capture(),
        imagesCaptor.capture(), userIdCaptor.capture());

    assertEquals(registerRequest, requestArgumentCaptor.getValue());
    assertEquals(4, imagesCaptor.getValue().size());
    assertEquals("banzzokee1.png", imagesCaptor.getValue().get(0).getOriginalFilename());
    assertEquals("banzzokee2.png", imagesCaptor.getValue().get(1).getOriginalFilename());
    assertEquals("banzzokee3.png", imagesCaptor.getValue().get(2).getOriginalFilename());
    assertEquals("banzzokee4.png", imagesCaptor.getValue().get(3).getOriginalFilename());
    assertEquals(1, userIdCaptor.getValue());
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

  @Test
  @DisplayName("분양게시글 등록 - 같은 파일명인 여러 파일을 업로드 할 경우 예외 처리")
  void registerAdoption_shouldThrowValidationError_whenFileHasDuplicateFilename()
      throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", registerRequest);

    //when
    MockMultipartFile image = MockDataUtil.createMockMultipartFile("images",
        "src/test/resources/images/banzzokee.png");
    MockMultipartHttpServletRequestBuilder post = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/adoptions")
        .part(mockPart)
        .file(image)
        .file(image);

    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));

  }

  @Test
  @DisplayName("분양게시글 등록 - 이미지 파일 8장 초과할 경우 예외 처리")
  void registerAdoption_shouldThrowValidationError_whenNumberOfFilesOver8()
      throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", registerRequest);

    //when
    MockMultipartHttpServletRequestBuilder postWithoutImage = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/adoptions")
        .part(mockPart);
    MockMultipartHttpServletRequestBuilder post = addImages(postWithoutImage, 9);
    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));

  }

  @Test
  @DisplayName("분양게시글 등록 - 이미지 파일이 아닐 경우 예외 처리")
  void registerAdoption_shouldThrowValidationError_whenFileIsNotImage()
      throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", registerRequest);

    //when
    MockMultipartFile textFile = createMockTextFile();
    MockMultipartHttpServletRequestBuilder post = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/adoptions")
        .part(mockPart)
        .file(textFile);

    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));

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

  @Test
  @DisplayName("분양게시글 등록 - request 필드 유효성 검사 실패")
  void registerAdoption_shouldThrowValidationError_whenInputInvalidRequest()
      throws Exception {
    String over50 = "over".repeat(50);
    String over500 = "over".repeat(500);
    AdoptionRegisterRequest request = AdoptionRegisterRequest.builder()
        .title(over50)  // 50자 이상
        .content(over500)  // 500자 이상
        .breed("잘못된 견종")  // 유효한 견종 아님경
        .size("기쁨형")  // 유효한 크기 아님
        .neutering(false)
        .gender("남성")  // 유효한 성별 아님
        .age(120)  // 100세 이상 입력 불가
        .healthChecked(true)
        .registeredAt("2024012301")  // 유효한 날짜 형식 아님
        .build();
    MockPart mockPart = MockDataUtil.createMockPart("request", request);

    //when
    MockMultipartHttpServletRequestBuilder postWithoutImage = MockMvcRequestBuilders
        .multipart(HttpMethod.POST, "/api/adoptions")
        .part(mockPart);
    MockMultipartHttpServletRequestBuilder post = addImages(postWithoutImage, 2);
    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));
  }

  @Test
  @DisplayName("분양게시글 상세조회 성공 테스트")
  void successGetAdoption() throws Exception {
    //given
    AdoptionResponse response = AdoptionResponse.builder()
        .adoptionId(1L)
        .title("강아지")
        .build();
    given(adoptionService.getAdoption(anyLong())).willReturn(response);
    //when
    MockMvcUtil.performGet(mockMvc, "/api/adoptions/1")
        .andExpect(jsonPath("$.adoptionId").value(1))
        .andExpect(jsonPath("$.title").value("강아지"));
    //then
  }

  @Test
  @DisplayName("분양게시글 수정 성공 테스트")
  void successUpdateAdoption() throws Exception {
    MockPart mockPart = MockDataUtil.createMockPart("request", updateRequest);

    //when
    MockMultipartHttpServletRequestBuilder putWithoutImage = MockMvcRequestBuilders
        .multipart(HttpMethod.PUT, "/api/adoptions/3")
        .part(mockPart);
    MockMultipartHttpServletRequestBuilder put = addImages(putWithoutImage, 4);
    ResultActions resultActions = mockMvc.perform(put).andDo(print());

    //then
    resultActions.andExpect(status().isOk());

    ArgumentCaptor<AdoptionUpdateRequest> requestArgumentCaptor = ArgumentCaptor.forClass(
        AdoptionUpdateRequest.class);
    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

    verify(adoptionService).updateAdoption(longArgumentCaptor.capture(),
        requestArgumentCaptor.capture(),
        imagesCaptor.capture(), longArgumentCaptor.capture());

    assertEquals(updateRequest, requestArgumentCaptor.getValue());
    assertEquals(3, longArgumentCaptor.getAllValues().get(0));
    assertEquals(1, longArgumentCaptor.getAllValues().get(1));
    assertEquals(4, imagesCaptor.getValue().size());
    assertEquals("banzzokee1.png", imagesCaptor.getValue().get(0).getOriginalFilename());
    assertEquals("banzzokee2.png", imagesCaptor.getValue().get(1).getOriginalFilename());
    assertEquals("banzzokee3.png", imagesCaptor.getValue().get(2).getOriginalFilename());
    assertEquals("banzzokee4.png", imagesCaptor.getValue().get(3).getOriginalFilename());
    assertEquals(1, longArgumentCaptor.getValue());
  }

  @Test
  @DisplayName("분양게시글 수정 - 같은 파일명인 여러 파일을 업로드 할 경우 예외 처리")
  void updateAdoption_shouldThrowValidationError_whenFileHasDuplicateFilename()
      throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", updateRequest);

    //when
    MockMultipartFile image = MockDataUtil.createMockMultipartFile("images",
        "src/test/resources/images/banzzokee.png");
    MockMultipartHttpServletRequestBuilder put = MockMvcRequestBuilders
        .multipart(HttpMethod.PUT, "/api/adoptions/1")
        .part(mockPart)
        .file(image)
        .file(image);

    ResultActions resultActions = mockMvc.perform(put).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));

  }

  @Test
  @DisplayName("분양게시글 수정 - 이미지 파일 8장 초과할 경우 예외 처리")
  void updateAdoption_shouldThrowValidationError_whenNumberOfFilesOver8()
      throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", updateRequest);

    //when
    MockMultipartHttpServletRequestBuilder putWithoutImage = MockMvcRequestBuilders
        .multipart(HttpMethod.PUT, "/api/adoptions/1")
        .part(mockPart);
    MockMultipartHttpServletRequestBuilder put = addImages(putWithoutImage, 9);
    ResultActions resultActions = mockMvc.perform(put).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));

  }

  @Test
  @DisplayName("분양게시글 수정 - 이미지 파일이 아닐 경우 예외 처리")
  void updateAdoption_shouldThrowValidationError_whenFileIsNotImage()
      throws Exception {
    //given
    MockPart mockPart = MockDataUtil.createMockPart("request", updateRequest);

    //when
    MockMultipartFile textFile = createMockTextFile();
    MockMultipartHttpServletRequestBuilder put = MockMvcRequestBuilders
        .multipart(HttpMethod.PUT, "/api/adoptions/1")
        .part(mockPart)
        .file(textFile);

    ResultActions resultActions = mockMvc.perform(put).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));

  }

  @Test
  @DisplayName("분양게시글 수정 - request 필드 유효성 검사 실패")
  void updateAdoption_shouldThrowValidationError_whenInputInvalidRequest()
      throws Exception {
    String over50 = "over".repeat(50);
    String over500 = "over".repeat(500);
    AdoptionUpdateRequest request = AdoptionUpdateRequest.builder()
        .title(over50)  // 50자 이상
        .content(over500)  // 500자 이상
        .breed("잘못된 견종")  // 유효한 견종 아님경
        .size("기쁨형")  // 유효한 크기 아님
        .neutering(false)
        .gender("남성")  // 유효한 성별 아님
        .age(120)  // 100세 이상 입력 불가
        .healthChecked(true)
        .registeredAt("2024012301")  // 유효한 날짜 형식 아님
        .build();
    MockPart mockPart = MockDataUtil.createMockPart("request", request);

    //when
    MockMultipartHttpServletRequestBuilder putWithoutImage = MockMvcRequestBuilders
        .multipart(HttpMethod.PUT, "/api/adoptions/1")
        .part(mockPart);
    MockMultipartHttpServletRequestBuilder put = addImages(putWithoutImage, 2);
    ResultActions resultActions = mockMvc.perform(put).andDo(print());

    //then
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("ARGUMENT_NOT_VALID"));
  }

  @Test
  @DisplayName("분양게시글 상태 변경 성공 테스트")
  void changeAdoptionStatus_success()
      throws Exception {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status("분양완료")
        .assignedUserId(1L)
        .build();
    // when & then
    mockMvc.perform(patch("/api/adoptions/2/status")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<AdoptionStatusChangeRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(AdoptionStatusChangeRequest.class);

    verify(adoptionService).changeAdoptionStatus(longArgumentCaptor.capture(),
        requestArgumentCaptor.capture(), longArgumentCaptor.capture());

    assertEquals(2L, longArgumentCaptor.getAllValues().get(0));
    assertEquals(1L, longArgumentCaptor.getAllValues().get(1));
    assertEquals(request, requestArgumentCaptor.getValue());
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 유효한 상태값이 아닌 경우(유효한 상태값 : 분양중, 예약중, 분양완료)")
  void changeAdoptionStatus_shouldThrowValidationError_whenInputInvalidRequest()
      throws Exception {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status("분양")
        .assignedUserId(1L)
        .build();
    // when & then
    mockMvc.perform(patch("/api/adoptions/2/status")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 분양완료로 변경하려는 경우 assignedUserId가 null인 경우")
  void changeAdoptionStatus_shouldThrowValidationError_whenChangeToFinishedWithAssignedUserIdNull()
      throws Exception {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status("분양완료")
        .assignedUserId(null)
        .build();
    // when & then
    mockMvc.perform(patch("/api/adoptions/2/status")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 예약중으로 변경하려는 경우 assignedUserId가 존재하는 경우")
  void changeAdoptionStatus_shouldThrowValidationError_whenChangeToResulvingWithAssignedUserId()
      throws Exception {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status("예약중")
        .assignedUserId(1L)
        .build();
    // when & then
    mockMvc.perform(patch("/api/adoptions/2/status")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("분양게시글 상태 변경 - 분양중으로 변경하려는 경우 assignedUserId가 존재하는 경우")
  void changeAdoptionStatus_shouldThrowValidationError_whenChangeToAdoptingWithAssignedUserId()
      throws Exception {
    //given
    AdoptionStatusChangeRequest request = AdoptionStatusChangeRequest.builder()
        .status("분양중")
        .assignedUserId(1L)
        .build();
    // when & then
    mockMvc.perform(patch("/api/adoptions/2/status")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

}