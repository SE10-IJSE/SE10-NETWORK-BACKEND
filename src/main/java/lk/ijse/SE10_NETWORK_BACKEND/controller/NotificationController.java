package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.NotificationDTO;
import lk.ijse.SE10_NETWORK_BACKEND.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/save")
    public ResponseEntity<String> saveNotification(@RequestBody NotificationDTO notificationDTO) {
        NotificationDTO dto = notificationService.saveNotification(notificationDTO);

        if (dto != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Notification saved successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Notification save failed");
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByStudentId(
            @PathVariable("id") Long userId,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("notificationCount") Integer notificationCount) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByStudentId(userId, pageNo, notificationCount);

        System.out.println(notifications);
        System.out.println(userId);

        if (notifications != null) {
            return ResponseEntity.status(HttpStatus.OK).body(notifications);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
