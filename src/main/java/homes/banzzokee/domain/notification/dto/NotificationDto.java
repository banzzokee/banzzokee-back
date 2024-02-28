package homes.banzzokee.domain.notification.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import homes.banzzokee.domain.notification.dto.serializer.StringToMapSerializer;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDto {

  private final Long id;

  @JsonSerialize(using = StringToMapSerializer.class)
  private final String message;

  private final LocalDateTime notifiedAt;

  public NotificationDto(Long id, String message, LocalDateTime notifiedAt) {
    this.id = id;
    this.message = message;
    this.notifiedAt = notifiedAt;
  }
}
