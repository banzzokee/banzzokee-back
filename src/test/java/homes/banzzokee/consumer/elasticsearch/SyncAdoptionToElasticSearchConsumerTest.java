package homes.banzzokee.consumer.elasticsearch;

import static homes.banzzokee.domain.type.AdoptionStatus.ADOPTING;
import static homes.banzzokee.domain.type.BreedType.RETRIEVER;
import static homes.banzzokee.domain.type.DogGender.FEMALE;
import static homes.banzzokee.domain.type.DogSize.LARGE;
import static homes.banzzokee.event.type.EntityAction.ADOPTION_CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import homes.banzzokee.consumer.error.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.EntityEvent;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncAdoptionToElasticSearchConsumerTest {

  @InjectMocks
  private SyncAdoptionToElasticSearchConsumer consumer;

  @Mock
  private AdoptionSearchRepository adoptionSearchRepository;

  @Mock
  private AdoptionRepository adoptionRepository;

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 분양 게시글이 존재하지 않으면 AdoptionNotFoundException 발생")
  void handleEvent_when_adoptionNotExists_then_throwAdoptionNotFoundException() {
    // given
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> consumer.handleEvent(EntityEvent.of(1L, ADOPTION_CREATED), null, null));
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 성공 검증")
  void handleEvent_when_success_then_verify() {
    // given
    User user = mock(User.class);
    given(user.getId()).willReturn(1L);
    given(user.getNickname()).willReturn("nickname");

    Adoption adoption = spy(Adoption.builder()
        .user(user)
        .title("title")
        .content("content")
        .breed(RETRIEVER)
        .size(LARGE)
        .neutering(true)
        .gender(FEMALE)
        .age(1)
        .healthChecked(true)
        .status(ADOPTING)
        .build());

    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    // when
    consumer.handleEvent(EntityEvent.of(1L, ADOPTION_CREATED), null, null);

    // then
    ArgumentCaptor<AdoptionDocument> adoptionDocumentCaptor =
        ArgumentCaptor.forClass(AdoptionDocument.class);
    verify(adoptionSearchRepository).save(adoptionDocumentCaptor.capture());

    AdoptionDocument adoptionDocument = adoptionDocumentCaptor.getValue();
    assertEquals(1L, adoptionDocument.getUserId());
    assertEquals("nickname", adoptionDocument.getUserNickname());
    assertEquals("title", adoptionDocument.getTitle());
    assertEquals("content", adoptionDocument.getContent());
    assertEquals(RETRIEVER, adoptionDocument.getBreed());
    assertEquals(LARGE, adoptionDocument.getSize());
    assertTrue(adoptionDocument.isNeutering());
    assertEquals(FEMALE, adoptionDocument.getGender());
    assertEquals(1, adoptionDocument.getAge());
    assertTrue(adoptionDocument.isHealthChecked());
    assertEquals(ADOPTING, adoptionDocument.getStatus());
  }
}