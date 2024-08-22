package lk.ijse.SE10_NETWORK_BACKEND.entity;

import jakarta.persistence.*;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    @Id
    private Long userId;

    private String name;

    private String email;

    private String password;

    private LocalDate dob;

    @Column (columnDefinition = "boolean default true")
    private boolean status;

    private String role="user";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    private List<Post> posts;

    @OneToMany(mappedBy = "user")
    private List<Inspire> inspires;


    public User(Long userId, String name, String email, String password, LocalDate dob) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.dob = dob;
    }

    public UserDTO toDto() {
        return new UserDTO(userId, name, email, password, dob);
    }

}
