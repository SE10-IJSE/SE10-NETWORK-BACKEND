package lk.ijse.SE10_NETWORK_BACKEND.repository;

import lk.ijse.SE10_NETWORK_BACKEND.entity.Inspire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InspireRepository extends JpaRepository<Inspire,Long> {
    @Query("SELECT i FROM Inspire i WHERE i.post.postId=:postId AND i.user.email=:email")
    Optional<Inspire> getInspiresByPostIdAndEmail(@Param("postId") Long postId, @Param("email") String email);
}