package homes.banzzokee.domain.room.dao;

import homes.banzzokee.domain.chat.dto.LastChatMessageDto;
import homes.banzzokee.domain.room.dto.ChatRoomDto;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.user.entity.User;
import java.awt.print.Pageable;
import java.util.Optional;
import org.springframework.data.domain.Page;

/**
 * CharRoom QueryDSL dao
 */
public interface ChatRoomQueryRepository {

  Page<ChatRoomDto> findAllByUser(User userChatRoom, Pageable pageable);
}
