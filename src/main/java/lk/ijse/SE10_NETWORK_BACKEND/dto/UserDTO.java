package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private String password;
    private LocalDate dob;
    private String role;

    public UserDTO(Long userId, String name, String email, String password, LocalDate dob) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.dob = dob;
    }
    public User toEntity() {
        return new User(userId, name, email, password, dob);
    }

}

