package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationDTO {
    private Long notificationId;
    private String content;
    private String type;
    private LocalDateTime createdAt;
    private Long userId;

    public NotificationDTO(String content, String type, Long userId) {
        this.content = content;
        this.type = type;
        this.userId = userId;
    }
}
