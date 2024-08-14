package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.NotificationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController {

    @PostMapping("/save")
    public ResponseEntity<String> saveNotification(@RequestBody NotificationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Notification saved successfully");
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByStudentId(
            @PathVariable("id") Long userId,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("notificationCount") Integer notificationCount) {
        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        return ResponseEntity.ok(notificationDTOS);
    }
}