package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordDTO implements Serializable {
    private String email;
    private String oldPassword;
    private String newPassword;
    private String otp;
}
