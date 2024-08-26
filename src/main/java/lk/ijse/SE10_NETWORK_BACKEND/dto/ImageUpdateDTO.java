package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ImageUpdateDTO implements Serializable {
    private String type;
    private MultipartFile image;
    private String email;
}
