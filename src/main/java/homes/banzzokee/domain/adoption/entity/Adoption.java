package homes.banzzokee.domain.adoption.entity;

import homes.banzzokee.domain.common.entity.BaseEntity;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 분양게시글 entity
 */
@Entity
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Adoption extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  private String content;

  @Enumerated(value = EnumType.STRING)
  private AdoptionStatus status;

  @Enumerated(value = EnumType.STRING)
  private BreedType breed;

  @Enumerated(value = EnumType.STRING)
  private DogSize size;

  private boolean neutering;

  @Enumerated(value = EnumType.STRING)
  private DogGender gender;

  private int age;

  private boolean healthChecked;

  private LocalDate registeredAt;

  // key를 filename, value를 이미지 저장 경로로 저장
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, String> images;

  private LocalDate adoptedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(referencedColumnName = "user_id", name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(referencedColumnName = "user_id", name = "assigned_user_id")
  private User assignedUser;

//  @OneToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "review_id")
//  private Review review;

  private LocalDateTime deletedAt;

  @Builder
  public Adoption(User user, String title, String content, BreedType breed, DogSize size,
      boolean neutering, DogGender gender, int age, boolean healthChecked,
      LocalDate registeredAt, Map<String, String> images, AdoptionStatus status) {
    this.user = user;
    this.title = title;
    this.content = content;
    this.breed = breed;
    this.size = size;
    this.neutering = neutering;
    this.gender = gender;
    this.age = age;
    this.healthChecked = healthChecked;
    this.registeredAt = registeredAt;
    this.images = images;
    this.status = status;
  }
}
