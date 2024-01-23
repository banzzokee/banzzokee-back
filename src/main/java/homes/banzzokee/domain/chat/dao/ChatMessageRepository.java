package homes.banzzokee.domain.chat.dao;

import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.room.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 채팅 메세지 repository
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  // todo: Slice 로 변경
  List<ChatMessage> findAllByRoom(ChatRoom room);
}
