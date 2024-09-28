package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationDTO implements Serializable {
    private Long notificationId;
    private String content;
    private String type;
    private LocalDateTime createdAt;
    private Long userId;
    private Long postId;
}
