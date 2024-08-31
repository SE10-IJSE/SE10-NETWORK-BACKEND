package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    private Long userId;

    private String name;

    private String email;

    private String batch;

    private LocalDate dob;

    private String bio;

    private String role;

    private String password;

    private String newPassword;

    private MultipartFile profilePic;

    private MultipartFile coverPic;

    private String profileImg;

    private String coverImg;
}
