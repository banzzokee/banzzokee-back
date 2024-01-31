package homes.banzzokee.domain.room.dao;

import homes.banzzokee.domain.chat.dto.LastChatMessageDto;
import homes.banzzokee.domain.room.entity.ChatRoom;
import java.util.Optional;

/**
 * CharRoom QueryDSL dao
 */
public interface ChatRoomQueryRepository {

  /**
   * 해당 채팅방 마지막 채팅 정보 추출
   *
   * @param room
   * @return
   */
  Optional<LastChatMessageDto> findLastChatMessageByRoom(ChatRoom room);
}
