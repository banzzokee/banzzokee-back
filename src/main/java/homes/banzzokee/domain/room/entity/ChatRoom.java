package homes.banzzokee.domain.room.entity;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 채팅방 entity
 */
@Entity
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "adoption_id")
  private Adoption adoption;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shelter_id")
  private Shelter shelter;

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;

  private LocalDateTime deletedAt;

  @Builder
  public ChatRoom(User user, Shelter shelter, Adoption adoption) {
    this.user = user;
    this.shelter = shelter;
    this.adoption = adoption;
  }

  public void leaveShelter() {
    this.shelter = null;
  }
}
