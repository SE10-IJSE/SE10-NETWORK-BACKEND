package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import lk.ijse.SE10_NETWORK_BACKEND.dto.NotificationDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Notification;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.exception.DataPersistFailedException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserNotAcceptableException;
import lk.ijse.SE10_NETWORK_BACKEND.repository.NotificationRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.NotificationService;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceIMPL implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    @Override
    public void saveNotification(String content, String token) {
        if (jwtUtil.getRoleFromToken(token).equals("ADMIN")) {
            try {
                Notification notification = new Notification();
                notification.setContent(content);
                notification.setType("System");
                notificationRepository.save(notification);
            } catch (Exception e) {
                throw new DataPersistFailedException("Failed to save notification");
            }
        } else throw new UserNotAcceptableException("User not an admin");
    }
    @Override
    public List<NotificationDTO> getNotificationsByStudentId(
            Long userId, Integer pageNo, Integer notificationCount, String token) {
        Pageable pageable = PageRequest.of(pageNo, notificationCount);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null && !user.getEmail().equals(jwtUtil.getUsernameFromToken(token))) {
            throw new UserNotAcceptableException("User not found or not acceptable");
        } else {
            Page<Notification> notificationsPage = notificationRepository.findByStudentId(userId, pageable);
            if (!notificationsPage.isEmpty()) {
                return notificationsPage.stream().map(notification -> new NotificationDTO(
                                notification.getNotificationId(),
                                notification.getContent(),
                                notification.getType(),
                                notification.getCreatedAt(),
                                notification.getUser() != null ? notification.getUser().getUserId() : null,
                                notification.getPost() != null ? notification.getPost().getPostId() : null
                        )
                ).collect(Collectors.toList());
            }
        }
        return null;
    }
}