package lk.ijse.SE10_NETWORK_BACKEND.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDTO implements Serializable {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    private String name;
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Email should be valid")
    private String email;
    @NotBlank(message = "Batch is required")
    private String batch;
    @NotNull(message = "Date of Birth is required")
    private LocalDate dob;
    @NotBlank(message = "Bio cannot be empty")
    @Size(max = 30, message = "Bio must be at most 30 characters")
    private String bio;
    private String role;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
    @NotBlank(message = "OTP cannot be empty")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 characters long")
    private String otp;
    private MultipartFile profilePic;
    private MultipartFile coverPic;
}