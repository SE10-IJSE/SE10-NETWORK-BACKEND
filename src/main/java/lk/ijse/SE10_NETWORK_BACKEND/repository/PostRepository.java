package lk.ijse.SE10_NETWORK_BACKEND.repository;

import lk.ijse.SE10_NETWORK_BACKEND.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.status='APPROVED' ORDER BY p.postId DESC")
    Page<Post> findAllPosts(Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.user.userId = :userId ORDER BY p.postId DESC")
    Page<Post> findByStudentId(@Param("userId") Long studentId, Pageable pageable);
    @Query("select p from Post p WHERE p.status='PENDING' AND p.user.email!=:email")
    Page<Post> findUnapprovedPosts(@Param("email") String email, Pageable pageable);
    @Query("select p from Post p WHERE p.user.email=:email AND p.status='APPROVED' ORDER BY p.postId DESC")
    Page<Post> findByEmail(@Param("email") String email, Pageable pageable);
}