package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostDTO implements Serializable {
    private Long postId;
    @NotBlank(message = "Content cannot be empty")
    @Size(max = 280, message = "Content cannot exceed 280 characters")
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String verifiedBy;
    private String userName;
    private String profileImg;
    private int inspirationCount;
    private boolean inspired;
    private String status;
}