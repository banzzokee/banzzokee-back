package homes.banzzokee.domain.bookmark.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import homes.banzzokee.domain.adoption.dto.AdoptionDto;
import homes.banzzokee.domain.bookmark.dto.BookmarkRegisterRequest;
import homes.banzzokee.domain.bookmark.service.BookmarkService;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.security.WithMockCustomUser;
import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(value = BookmarkController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = ASSIGNABLE_TYPE,
            classes = {JwtAuthenticationFilter.class})
    })
class BookmarkControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private BookmarkService bookmarkService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @WithMockCustomUser
  @DisplayName("[북마크 등록] - 성공 검증")
  void registerBookmark_validInput_then_success() throws Exception {
    // given
    long bookmarkId = 1L;
    BookmarkRegisterRequest request = BookmarkRegisterRequest.builder()
        .adoptionId(1L)
        .build();
    doNothing().when(bookmarkService).registerBookmark(
        any(UserDetailsImpl.class), any(BookmarkRegisterRequest.class));

    // when
    ResultActions resultActions = mockMvc.perform(post("/api/bookmarks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)));

    // then
    verify(bookmarkService).registerBookmark(
        any(UserDetailsImpl.class), any(BookmarkRegisterRequest.class));
    resultActions.andExpect(status().isCreated())
        .andExpect(
            header().string("Location", containsString("/api/bookmarks/" + bookmarkId)));
  }

  @Test
  @WithMockCustomUser
  @DisplayName("[북마크 삭제] - 성공 검증")
  void deleteBookmark_when_valid_then_success() throws Exception {
    // given
    long bookmarkId = 1L;

    // when & then
    ResultActions resultActions = mockMvc.perform(
        delete("/api/bookmarks/{bookmarkId}", bookmarkId));

    resultActions.andExpect(status().isOk());
    verify(bookmarkService).deleteBookmark(any(UserDetailsImpl.class), eq(bookmarkId));
  }

  @Test
  @WithMockCustomUser
  @DisplayName("[북마크 전체 조회] - 성공 검증")
  void findAllBookmark_when_valid_then_success() throws Exception {
    // given
    Pageable pageable = PageRequest.of(0, 10,
        Sort.by(Direction.fromString("desc"), "createdAt"));
    AdoptionDto adoptionDto1 = AdoptionDto.builder()
        .userNickname("반쪽이").adoptionId(1L)
        .title("반쪽이입니다.").content("분양합니다.")
        .imageUrls(List.of("url1")).breed(BreedType.POODLE)
        .size(DogSize.SMALL).neutering(true).gender(DogGender.MALE)
        .age(1).healthChecked(true).registeredAt(LocalDate.of(2024, 2, 8))
        .status(AdoptionStatus.ADOPTING).adoptedAt(LocalDate.of(2024, 2, 8))
        .createdAt(LocalDateTime.of(2024, 2, 8, 1, 0))
        .updatedAt(LocalDateTime.of(2024, 2, 8, 1, 0))
        .build();
    List<AdoptionDto> adoptionDtoList = Collections.singletonList(adoptionDto1);
    Slice<AdoptionDto> adoptionsSlice = new SliceImpl<>(adoptionDtoList, pageable, true);
    UserDetailsImpl userDetailsMock = (UserDetailsImpl) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    given(bookmarkService.findAllBookmark(any(UserDetailsImpl.class),
        eq(pageable))).willReturn(adoptionsSlice);

    // when
    ResultActions resultActions = mockMvc.perform(get("/api/bookmarks/adoptions"));

    // then
    verify(bookmarkService).findAllBookmark(userDetailsMock, pageable);
    resultActions.andExpect(status().isOk())
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(adoptionDtoList.size()))
        .andExpect(jsonPath("$.content[0].userId").value(adoptionDto1.getUserId()))
        .andExpect(
            jsonPath("$.content[0].userNickname").value(adoptionDto1.getUserNickname()))
        .andExpect(
            jsonPath("$.content[0].adoptionId").value(adoptionDto1.getAdoptionId()))
        .andExpect(jsonPath("$.content[0].title").value(adoptionDto1.getTitle()))
        .andExpect(jsonPath("$.content[0].content").value(adoptionDto1.getContent()))
        .andExpect(jsonPath("$.content[0].breed.value").value(
            adoptionDto1.getBreed().getValue()))
        .andExpect(
            jsonPath("$.content[0].size.value").value(adoptionDto1.getSize().getValue()))
        .andExpect(jsonPath("$.content[0].neutering").value(adoptionDto1.isNeutering()))
        .andExpect(jsonPath("$.content[0].gender.value").value(
            adoptionDto1.getGender().getValue()))
        .andExpect(jsonPath("$.content[0].age").value(adoptionDto1.getAge()))
        .andExpect(
            jsonPath("$.content[0].healthChecked").value(adoptionDto1.isHealthChecked()))
        .andExpect(jsonPath("$.content[0].registeredAt").exists())
        .andExpect(jsonPath("$.content[0].status.value").value(
            adoptionDto1.getStatus().getValue()))
        .andExpect(jsonPath("$.content[0].adoptedAt").exists())
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.content[0].updatedAt").exists())
        .andDo(print());
  }
}