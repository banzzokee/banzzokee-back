package homes.banzzokee.domain.room.dao;

import static homes.banzzokee.domain.adoption.entity.QAdoption.adoption;
import static homes.banzzokee.domain.chat.entity.QChatMessage.chatMessage;
import static homes.banzzokee.domain.room.entity.QChatRoom.chatRoom;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import homes.banzzokee.domain.chat.dto.LastChatMessageDto;
import homes.banzzokee.domain.room.dto.ChatAdoptionDto;
import homes.banzzokee.domain.room.dto.ChatRoomDto;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.user.entity.User;
import jakarta.transaction.Transactional;
import java.awt.print.Pageable;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
  public Page<ChatRoomDto> findAllByUser(User userChatRoom, Pageable pageable) {
    return Optional.ofNullable(query
        .select(
            Projections.fields(ChatRoomDto.class,
                chatRoom.id.as("roomId"),
                Projections.fields(ChatAdoptionDto.class,
                    adoption.id.as("adoptionId"),
                    adoption.title.as("title"),
                    adoption.content.as("content")

                    ),
                chatRoom.lastMessage,
                chatRoom.lastMessageType,
                chatRoom.lastMessageCreatedAt
            )
        )
        .from(chatMessage)
        .orderBy(chatMessage.id.desc())
        .where(chatMessage.room.eq(room))
        .fetchFirst()
    );
  }


}
