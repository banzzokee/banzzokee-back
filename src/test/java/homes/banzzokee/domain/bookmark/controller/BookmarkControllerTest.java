package homes.banzzokee.domain.bookmark.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import homes.banzzokee.domain.bookmark.dto.BookmarkRegisterRequest;
import homes.banzzokee.domain.bookmark.service.BookmarkService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
  void sendRegisterBookmark_validInput_then_success() throws Exception {
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
        .andExpect(header().string("Location", containsString("/api/bookmarks/" + bookmarkId)));
  }
}