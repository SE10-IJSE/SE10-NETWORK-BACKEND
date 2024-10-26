package lk.ijse.SE10_NETWORK_BACKEND.controller;

import jakarta.validation.Valid;
import lk.ijse.SE10_NETWORK_BACKEND.dto.NotificationDTO;
import lk.ijse.SE10_NETWORK_BACKEND.exception.DataPersistFailedException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserNotAcceptableException;
import lk.ijse.SE10_NETWORK_BACKEND.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;
    /**
     * Saves a new notification for a user.
     *
     * @param content The notification content to be saved.
     * @param token JWT token extracted from the Authorization header.
     * @return A ResponseEntity indicating the result of the operation with status 201 (Created) on success, 406 (Not Acceptable) if the user is not acceptable, and 500 (Internal Server Error) on data persistence failure.
     */
    @PostMapping
    public ResponseEntity<Void> saveNotification(
            @Valid @RequestBody String content,
            @RequestHeader("Authorization") String token) {
        log.info("Saving notification for user. Token received, proceeding with save.");
        try {
            notificationService.saveNotification(content, token.substring(7));
            log.info("Notification saved successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UserNotAcceptableException e) {
            log.error("User not acceptable. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        } catch (DataPersistFailedException e) {
            log.error("Failed to persist notification data. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /**
     * Retrieves notifications for a specific user, paginated by page number and notification count.
     *
     * @param userId            The ID of the user whose notifications are to be retrieved.
     * @param pageNo            The page number to retrieve.
     * @param notificationCount The number of notifications to retrieve per page.
     * @param token             JWT token extracted from the Authorization header.
     * @return A ResponseEntity with status 200 (OK) and a list of notifications on success, 404 (Not Found) if no notifications are found, and 406 (Not Acceptable) if the user is not acceptable.
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByStudentId(
            @PathVariable("id") Long userId,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("notificationCount") Integer notificationCount,
            @RequestHeader("Authorization") String token) {
        log.info("Retrieving notifications for user ID: {}. Page number: {}, Count: {}", userId, pageNo, notificationCount);
        try {
            List<NotificationDTO> notifications =
                    notificationService.getNotificationsByStudentId(userId, pageNo, notificationCount, token.substring(7));
            if (notifications != null && !notifications.isEmpty()) {
                log.info("Notifications retrieved successfully for user ID: {}", userId);
                return ResponseEntity.status(HttpStatus.OK).body(notifications);
            } else {
                log.warn("No notifications found for user ID: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (UserNotAcceptableException e) {
            log.error("User ID: {} is not acceptable. Error: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
    }
}