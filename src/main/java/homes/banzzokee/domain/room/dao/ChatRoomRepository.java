package homes.banzzokee.domain.room.dao;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 채팅방 repository
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>,
    ChatRoomQueryRepository {

  List<ChatRoom> findAllByShelterId(long shelterId);

  List<ChatRoom> findAllByUser(User user);

  boolean existsByUserAndAdoption(User user, Adoption adoption);


}
