package homes.banzzokee.domain.bookmark.entity;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Bookmark {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "adoption_id")
  private Adoption adoption;

  @Builder
  public Bookmark(User user, Adoption adoption) {
    this.user = user;
    this.adoption = adoption;
  }
}
