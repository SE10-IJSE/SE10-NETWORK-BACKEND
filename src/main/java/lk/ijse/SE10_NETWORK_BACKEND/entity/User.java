package lk.ijse.SE10_NETWORK_BACKEND.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private Long userId;
    @Column(length = 150, nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(length = 10, nullable = false)
    private String batch;
    @Column(nullable = false)
    private String password;
    private LocalDate dob;
    @Column(length = 10, nullable = false)
    private String status = "Active";
    @Column(length = 30)
    private String bio;
    @Column(length = 20, nullable = false)
    private String role;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inspire> inspires;
}