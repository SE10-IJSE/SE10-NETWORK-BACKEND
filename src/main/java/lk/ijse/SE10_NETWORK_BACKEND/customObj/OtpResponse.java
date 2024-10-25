package lk.ijse.SE10_NETWORK_BACKEND.customObj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OtpResponse {
    private String otp;
    private String username;
}