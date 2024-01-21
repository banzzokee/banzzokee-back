package homes.banzzokee.domain.shelter.dao;

import homes.banzzokee.domain.shelter.entity.Shelter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long> {

}
