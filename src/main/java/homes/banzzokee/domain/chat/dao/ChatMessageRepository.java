package homes.banzzokee.domain.chat.dao;

import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.room.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 채팅 메세지 repository
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  Page<ChatMessage> findAllByRoom(ChatRoom room, Pageable pageable);
}
