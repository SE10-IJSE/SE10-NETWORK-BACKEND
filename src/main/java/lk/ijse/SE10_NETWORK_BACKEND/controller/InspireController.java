package lk.ijse.SE10_NETWORK_BACKEND.controller;

import jakarta.validation.Valid;
import lk.ijse.SE10_NETWORK_BACKEND.dto.InspireDTO;
import lk.ijse.SE10_NETWORK_BACKEND.exception.DataPersistFailedException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.InspireNotFoundException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.InvalidInspirationException;
import lk.ijse.SE10_NETWORK_BACKEND.service.InspireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inspire")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class InspireController {
    private final InspireService inspireService;
    /**
     * Handles the request to save an inspiration (like) for a specific post.
     *
     * @param dto   The data transfer object containing inspiration details.
     * @param token The JWT token used to authenticate the user.
     * @return A ResponseEntity indicating the result of the operation with status 201 (Created) on success, and 400 (Bad Request) on failure.
     */
    @PostMapping
    public ResponseEntity<String> saveInspire(
            @Valid @RequestBody InspireDTO dto,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("Saving inspiration for post with ID: {}", dto.getPostId());
            inspireService.saveInspiration(dto, token);
            log.info("Inspiration saved successfully for post ID: {}", dto.getPostId());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (InvalidInspirationException e) {
            log.warn("Invalid inspiration attempt. Post or user not found for post ID: {}", dto.getPostId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (DataPersistFailedException e) {
            log.error("Data persistence failed while saving inspiration for post ID: {}", dto.getPostId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    /**
     * Handles the request to delete an inspiration (like) from a specific post.
     *
     * @param postId The ID of the post to remove the inspiration from.
     * @param token  The JWT token used to authenticate the user.
     * @return A ResponseEntity indicating the result of the operation with status 204 (No Content) on success, and 404 (Not Found) if the inspiration is not found.
     */
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Void> deleteInspire(
            @PathVariable("postId") Long postId,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("Deleting inspiration for post with ID: {}", postId);
            inspireService.deleteInspiration(postId, token);
            log.info("Inspiration deleted successfully for post ID: {}", postId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (InspireNotFoundException e) {
            log.warn("No inspiration found to delete for post ID: {}", postId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}