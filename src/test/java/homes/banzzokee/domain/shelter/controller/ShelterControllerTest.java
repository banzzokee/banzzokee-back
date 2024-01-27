package homes.banzzokee.domain.shelter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.shelter.dto.ShelterRegisterRequest;
import homes.banzzokee.domain.shelter.service.ShelterService;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.global.util.MockMvcUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(value = ShelterController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ShelterControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ShelterService shelterService;

  @Test
  @DisplayName("[보호소 등록] - 요청 데이터가 서비스 메서드에 잘 들어가는지 검증")
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
    MockMultipartFile mockFile = MockDataUtil.createMockMultipartFile(
        "src/test/resources/images/banzzokee.png");

    // when
    MockMultipartHttpServletRequestBuilder post = MockMvcRequestBuilders
        .multipart(POST, "/api/shelters?userId=1")
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
  @DisplayName("[보호소 승인] - 요청 데이터가 서비스 메서드에 잘 들어가는지 검증")
  void verifyShelter_when_validInput_then_success() throws Exception {
    // when
    ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
        "/api/shelters/2/verify?userId=1", null);

    // then
    resultActions.andExpect(status().isOk());

    ArgumentCaptor<Long> shelterIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

    verify(shelterService)
        .verifyShelter(shelterIdCaptor.capture(), userIdCaptor.capture());

    assertEquals(2L, shelterIdCaptor.getValue());
    assertEquals(1L, userIdCaptor.getValue());
  }
}