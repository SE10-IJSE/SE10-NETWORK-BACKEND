package lk.ijse.SE10_NETWORK_BACKEND.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ImageUpdateDTO implements Serializable {
    @NotBlank(message = "Type is required")
    private String type;
    private MultipartFile image;
}