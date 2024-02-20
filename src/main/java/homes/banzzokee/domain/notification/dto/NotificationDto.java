package homes.banzzokee.domain.notification.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationDto {

  private final Long id;
  private final String title;
  private final String body;
  private final LocalDateTime notifiedAt;
}
