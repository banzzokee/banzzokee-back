package homes.banzzokee.domain.bookmark.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class BookmarkRegisterRequest {

  @NotNull(message = "adoptionId 는 필수 입력 값 입니다.")
  private final Long adoptionId;

}
