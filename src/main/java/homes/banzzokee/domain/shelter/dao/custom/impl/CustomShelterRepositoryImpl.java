package homes.banzzokee.domain.shelter.dao.custom.impl;

import static homes.banzzokee.domain.shelter.entity.QShelter.shelter;
import static homes.banzzokee.domain.user.entity.QFollow.follow;

import com.querydsl.jpa.impl.JPAQueryFactory;
import homes.banzzokee.domain.shelter.dao.custom.CustomShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomShelterRepositoryImpl implements
    CustomShelterRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Shelter> findAllByFollower(Long userId) {
    return queryFactory
        .selectFrom(shelter)
        .innerJoin(follow)
        .on(shelter.user.id.eq(follow.followee.id)).fetchJoin()
        .where(follow.follower.id.eq(userId))
        .fetch();
  }
}
