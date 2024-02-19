package homes.banzzokee.domain.review.entity;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.entity.convertor.ImagesConvertor;
import homes.banzzokee.domain.common.entity.BaseEntity;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "adoption_id")
  private Adoption adoption;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  private String title;

  private String content;

  @Convert(converter = ImagesConvertor.class)
  @Column(length = 1000)
  private List<S3Object> images;

  private LocalDateTime deletedAt;

  @Builder
  public Review(Adoption adoption, User user, String title, String content, List<S3Object> images) {
    this.adoption = adoption;
    this.user = user;
    this.title = title;
    this.content = content;
    this.images = images;
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }

  public void update(String title, String content, List<S3Object> images) {
    this.title = title;
    this.content = content;
    this.images = images;
  }
}
