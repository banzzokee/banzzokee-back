package homes.banzzokee.domain.user.dao;

import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmailAndDeletedAtIsNull(String email);

  User findByShelter(Shelter shelter);

  Optional<User> findByEmailAndDeletedAtNull(String email);

  boolean existsByNicknameAndDeletedAtIsNull(String nickname);

  Optional<User> findByEmail(String email);
}
