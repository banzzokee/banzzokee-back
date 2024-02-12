package homes.banzzokee.domain.adoption.entity;

import homes.banzzokee.domain.adoption.entity.convertor.ImagesConvertor;
import homes.banzzokee.domain.common.entity.BaseEntity;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

  @Convert(converter = ImagesConvertor.class)
  @Column(length = 1000)
  private List<S3Object> images;

  private LocalDate adoptedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(referencedColumnName = "id", name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(referencedColumnName = "id", name = "assigned_user_id")
  private User assignedUser;

  // Todo : Review Entity 연결
//  @OneToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "review_id")
//  private Review review;

  private LocalDateTime deletedAt;

  @Builder
  public Adoption(User user, String title, String content, BreedType breed, DogSize size,
      boolean neutering, DogGender gender, int age, boolean healthChecked,
      LocalDate registeredAt, List<S3Object> images, AdoptionStatus status) {
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

  public void updateAdoption(String title, String content, BreedType breed, DogSize size,
      boolean neutering, DogGender gender, int age, boolean healthChecked,
      LocalDate registeredAt, List<S3Object> images) {
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
  }

  public void updateStatusToFinish(AdoptionStatus status, User assignedUser) {
    this.status = status;
    this.assignedUser = assignedUser;
    this.adoptedAt = LocalDate.now();
  }

  public void updateStatusExceptToFinish(AdoptionStatus status) {
    this.status = status;
    this.assignedUser = null;
    this.adoptedAt = null;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }
}
