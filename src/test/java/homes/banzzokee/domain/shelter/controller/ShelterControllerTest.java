package homes.banzzokee.domain.shelter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.shelter.dto.ShelterRegisterRequest;
import homes.banzzokee.domain.shelter.dto.ShelterUpdateRequest;
import homes.banzzokee.domain.shelter.dto.ShelterUpdateResponse;
import homes.banzzokee.domain.shelter.service.ShelterService;
import homes.banzzokee.global.security.WithMockCustomUser;
import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.global.util.MockMvcUtil;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(value = ShelterController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = ASSIGNABLE_TYPE,
            classes = {JwtAuthenticationFilter.class})
    })
@AutoConfigureMockMvc(addFilters = false)
class ShelterControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ShelterService shelterService;

  @Test
  @DisplayName("[보호소 등록] - 성공 검증")
  @WithMockCustomUser
  void registerShelter_when_validInput_then_success() throws Exception {
    // given
    ShelterRegisterRequest request = ShelterRegisterRequest.builder()
        .name("보호소")
        .description("설명")
        .tel("02-1234-5678")
        .address("주소")
        .latitude(37.0)
        .longitude(131.0)
        .build();
    MockPart mockPart = MockDataUtil.createMockPart("request", request);
    MockMultipartFile mockFile = MockDataUtil.createMockMultipartFile("shelterImg",
        "src/test/resources/images/banzzokee.png");

    // when
    MockMultipartHttpServletRequestBuilder post = MockMvcRequestBuilders
        .multipart(POST, "/api/shelters")
        .file("shelterImg", mockFile.getBytes())
        .part(mockPart);
    ResultActions resultActions = mockMvc.perform(post).andDo(print());

    // then
    resultActions.andExpect(status().isOk());

    ArgumentCaptor<ShelterRegisterRequest> requestCaptor
        = ArgumentCaptor.forClass(ShelterRegisterRequest.class);
    ArgumentCaptor<MultipartFile> fileCaptor
        = ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

    verify(shelterService)
        .registerShelter(requestCaptor.capture(),
            fileCaptor.capture(),
            userIdCaptor.capture());

    assertEquals(request, requestCaptor.getValue());
    assertEquals(mockFile.getSize(), fileCaptor.getValue().getSize());
    assertEquals(1L, userIdCaptor.getValue());
  }

  @Test
  @DisplayName("[보호소 승인] - 성공 검증")
  @WithMockCustomUser
  void verifyShelter_when_validInput_then_success() throws Exception {
    // when
    ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
        "/api/shelters/2/verify", null);

    // then
    resultActions.andExpect(status().isOk());

    ArgumentCaptor<Long> shelterIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

    verify(shelterService)
        .verifyShelter(shelterIdCaptor.capture(), userIdCaptor.capture());

    assertEquals(2L, shelterIdCaptor.getValue());
    assertEquals(1L, userIdCaptor.getValue());
  }

  @Test
  @DisplayName("[보호소 수정] - 성공 검증")
  @WithMockCustomUser
  void updateShelter_when_validInput_then_success() throws Exception {
    // given
    ShelterUpdateRequest request = ShelterUpdateRequest.builder()
        .name("name")
        .description("description")
        .tel("02-1234-5678")
        .address("서울시")
        .latitude(37.0)
        .longitude(121.0)
        .build();
    MockPart mockPart = MockDataUtil.createMockPart("request", request);
    MockMultipartFile mockFile = MockDataUtil.createMockMultipartFile("shelterImg",
        "src/test/resources/images/banzzokee.png");

    MockMultipartHttpServletRequestBuilder patch = MockMvcRequestBuilders
        .multipart(PATCH, "/api/shelters/1")
        .file(mockFile)
        .part(mockPart);

    given(shelterService.updateShelter(eq(1L), eq(request), any(MultipartFile.class),
        eq(1L)))
        .willReturn(ShelterUpdateResponse.builder()
            .shelterId(1L)
            .name(request.getName())
            .description(request.getDescription())
            .tel(request.getTel())
            .address(request.getAddress())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .updatedAt(LocalDateTime.now())
            .shelterImgUrl(mockFile.getOriginalFilename())
            .build());

    // when
    ResultActions resultActions = mockMvc.perform(patch).andDo(print());

    // then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.shelterId").value(1))
        .andExpect(jsonPath("$.name").value("name"))
        .andExpect(jsonPath("$.description").value("description"))
        .andExpect(jsonPath("$.tel").value("02-1234-5678"))
        .andExpect(jsonPath("$.address").value("서울시"))
        .andExpect(jsonPath("$.latitude").value(37.0))
        .andExpect(jsonPath("$.longitude").value(121.0))
        .andExpect(jsonPath("$.shelterImgUrl").value(mockFile.getOriginalFilename()));

    ArgumentCaptor<Long> shelterIdCaptor
        = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<ShelterUpdateRequest> requestCaptor
        = ArgumentCaptor.forClass(ShelterUpdateRequest.class);
    ArgumentCaptor<MultipartFile> shelterImageCaptor
        = ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<Long> userIdCaptor
        = ArgumentCaptor.forClass(Long.class);

    verify(shelterService)
        .updateShelter(shelterIdCaptor.capture(),
            requestCaptor.capture(),
            shelterImageCaptor.capture(),
            userIdCaptor.capture());

    assertEquals(1L, shelterIdCaptor.getValue());
    assertEquals(1L, userIdCaptor.getValue());
    assertEquals(request, requestCaptor.getValue());
    assertEquals(mockFile.getSize(), shelterImageCaptor.getValue().getSize());
    assertEquals(mockFile.getOriginalFilename(),
        shelterImageCaptor.getValue().getOriginalFilename());
  }

  @Test
  @DisplayName("[보호소 삭제] - 성공 검증")
  @WithMockCustomUser
  void unregisterShelter_when_validInput_then_success() throws Exception {
    // given
    // when
    ResultActions resultActions = MockMvcUtil.performDelete(mockMvc,
        "/api/shelters/1");

    // then
    resultActions.andExpect(status().isOk());

    ArgumentCaptor<Long> shelterIdCaptor
        = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Long> userIdCaptor
        = ArgumentCaptor.forClass(Long.class);

    verify(shelterService)
        .unregisterShelter(shelterIdCaptor.capture(), userIdCaptor.capture());

    assertEquals(1L, shelterIdCaptor.getValue());
    assertEquals(1L, userIdCaptor.getValue());
  }
}