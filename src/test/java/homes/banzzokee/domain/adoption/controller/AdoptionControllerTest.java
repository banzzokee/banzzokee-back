package homes.banzzokee.domain.adoption.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.RequestEntity.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import homes.banzzokee.domain.adoption.dto.AdoptionResponse;
import homes.banzzokee.domain.adoption.service.AdoptionService;
import homes.banzzokee.domain.user.controller.UserController;
import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import homes.banzzokee.global.util.MockMvcUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AdoptionController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AdoptionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AdoptionService adoptionService;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

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

}