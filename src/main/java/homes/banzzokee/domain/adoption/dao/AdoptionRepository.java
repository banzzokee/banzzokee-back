package homes.banzzokee.domain.adoption.dao;

import homes.banzzokee.domain.adoption.entity.Adoption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdoptionRepository extends JpaRepository<Adoption, Long> {

}
