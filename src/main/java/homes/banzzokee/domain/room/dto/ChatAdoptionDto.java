package homes.banzzokee.domain.room.dto;

import homes.banzzokee.domain.adoption.entity.Adoption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅 반환 입양글
 */
@Getter
@AllArgsConstructor
@Builder
public class ChatAdoptionDto {

  private final Long adoptionId;

  /**
   * 입양글 제목
   */
  private final String title;

  /**
   * 입양글 내용
   */
  private final String content;

  /**
   * 첫번째 사진
   */
  private final String firstImgUrl;

  public static ChatAdoptionDto fromEntity(Adoption adoption) {
    return ChatAdoptionDto.builder()
        .adoptionId(adoption.getId())
        .title(adoption.getTitle())
        .content(adoption.getContent())
        .firstImgUrl(adoption.getImages().get(0).getUrl())
        .build();
  }

}
