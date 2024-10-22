package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserSearchDTO {
    private String name;
    private String email;
    private String batch;
    private String profileImg;
}
