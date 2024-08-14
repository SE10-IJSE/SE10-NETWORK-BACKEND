package lk.ijse.SE10_NETWORK_BACKEND.dto;

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
}

