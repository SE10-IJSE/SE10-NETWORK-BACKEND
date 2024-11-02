package lk.ijse.SE10_NETWORK_BACKEND.service;

import lk.ijse.SE10_NETWORK_BACKEND.dto.NotificationDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NotificationService {
    void saveNotification(String content, String token);
    List<NotificationDTO> getNotificationsByStudentId(Long userId, Integer pageNo, Integer notificationCount, String token);
}