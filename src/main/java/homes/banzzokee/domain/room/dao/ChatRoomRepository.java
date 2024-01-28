package homes.banzzokee.domain.room.dao;

import homes.banzzokee.domain.room.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 채팅방 repository
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  List<ChatRoom> findAllByShelterId(long shelterId);
}
