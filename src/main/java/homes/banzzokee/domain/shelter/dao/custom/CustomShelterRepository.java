package homes.banzzokee.domain.shelter.dao.custom;

import homes.banzzokee.domain.shelter.entity.Shelter;
import java.util.List;

public interface CustomShelterRepository {

  List<Shelter> findAllByFollower(Long userId);
}
