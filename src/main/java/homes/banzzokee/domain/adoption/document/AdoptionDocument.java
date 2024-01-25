package homes.banzzokee.domain.adoption.document;

import homes.banzzokee.domain.user.dto.UserProfileDto;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Document(indexName = "adoption")
@Setting(settingPath = "/elasticsearch/adoption-setting.json")
@Mapping(mappingPath = "/elasticsearch/adoption-mapping.json")
public class AdoptionDocument {

  @Id
  private Long id;

  private String title;

  private String content;

  private String status;

  private String breed;

  private String size;

  private boolean neutering;

  private String gender;

  private int age;

  private boolean healthChecked;

  private LocalDate registeredAt;

  private List<String> images;

  private UserProfileDto user;

  private UserProfileDto assignedUser;

//  private ReviewDto review;

  private LocalDateTime deletedAt;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

}
