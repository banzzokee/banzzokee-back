package homes.banzzokee.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class ReviewUpdateRequest {

  @NotBlank @Size(max = 50)
  private final String title;

  @NotBlank @Size(max = 500)
  private final String content;

}
