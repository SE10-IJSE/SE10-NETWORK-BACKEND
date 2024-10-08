package lk.ijse.SE10_NETWORK_BACKEND.repository;

import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE FUNCTION('MONTH', u.dob) = FUNCTION('MONTH', CURRENT_DATE) AND FUNCTION('DAY', u.dob) = FUNCTION('DAY', CURRENT_DATE)")
    Optional<List<User>> findUsersWithBirthday();

    @Query("SELECT u FROM User u WHERE u.status='Active' AND u.email=:email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.name = :name OR u.name LIKE :name% OR u.name LIKE %:name")
    Page<User> findUsersByNameOrNameLike(@Param("name") String name, Pageable pageable);

    boolean existsByEmail(String userName);
}
