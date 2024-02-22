package homes.banzzokee.domain.shelter.dao;

import homes.banzzokee.domain.shelter.dao.custom.CustomShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long>,
    CustomShelterRepository {

  Optional<Shelter> findByIdAndDeletedAtIsNull(Long shelterId);

  Shelter getByUser(User user);
}
