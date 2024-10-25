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
public class NotificationDTO implements Serializable {
    private Long notificationId;
    @NotBlank(message = "Content cannot be empty")
    @Size(max = 100, message = "Content cannot exceed 100 characters")
    private String content;
    @Size(max = 30, message = "Type cannot exceed 30 characters")
    private String type;
    private LocalDateTime createdAt;
    private Long userId;
    private Long postId;
}