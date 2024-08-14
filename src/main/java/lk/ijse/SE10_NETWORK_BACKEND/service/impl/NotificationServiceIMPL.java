package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import lk.ijse.SE10_NETWORK_BACKEND.dto.NotificationDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Notification;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.repository.NotificationRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceIMPL implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public NotificationDTO saveNotification(NotificationDTO dto) {
        User user = userRepository.findById(dto.getUserId()).orElse(null);

        if (user != null) {
            Notification notification = new Notification(
                    dto.getContent(),
                    dto.getType(),
                    user
            );
            notificationRepository.save(notification);
            return dto;
        }
        return null;
    }

    @Override
    public List<NotificationDTO> getNotificationsByStudentId(Long userId, Integer pageNo, Integer notificationCount) {
        Pageable pageable = PageRequest.of(pageNo, notificationCount);
        Page<Notification> notificationsPage = notificationRepository.findByStudentId(userId, pageable);

        if (!notificationsPage.isEmpty()) {
            return notificationsPage.stream().map(notification -> new NotificationDTO(
                            notification.getNotificationId(),
                            notification.getContent(),
                            notification.getType(),
                            notification.getCreatedAt(),
                            notification.getUser().getUserId()
                    )
            ).collect(Collectors.toList());
        }
        return null;
    }

}
