package lk.ijse.SE10_NETWORK_BACKEND.repository;

import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE FUNCTION('DATE', u.dob) = CURRENT_DATE")
    Optional<List<User>> findUsersWithBirthday();

    @Query("SELECT u FROM User u WHERE u.email=:email AND u.password=:password")
    Optional<User> findByUsernameAndPassword(@Param("email") String username, @Param("password") String password);
}
