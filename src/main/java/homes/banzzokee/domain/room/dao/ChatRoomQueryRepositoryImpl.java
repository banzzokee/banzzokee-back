package homes.banzzokee.domain.room.dao;

import static homes.banzzokee.domain.chat.entity.QChatMessage.chatMessage;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import homes.banzzokee.domain.chat.dto.LastChatMessageDto;
import homes.banzzokee.domain.room.entity.ChatRoom;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ChatRoom QueryDSL 구현
 */
@Repository
@Transactional
@RequiredArgsConstructor
public class ChatRoomQueryRepositoryImpl implements ChatRoomQueryRepository {

  private final JPAQueryFactory query;

  @Override
  public Optional<LastChatMessageDto> findLastChatMessageByRoom(ChatRoom room) {
    return Optional.ofNullable(query
        .select(
            Projections.fields(LastChatMessageDto.class,
                chatMessage.room,
                chatMessage.id.as("messageId"),
                chatMessage.message.as("lastMessage"),
                chatMessage.messageType.as("lastMessageType"),
                chatMessage.createdAt.as("lastMessageCreatedAt")
            )
        )
        .from(chatMessage)
        .orderBy(chatMessage.id.desc())
        .where(chatMessage.room.eq(room))
        .fetchFirst()
    );
  }


}
