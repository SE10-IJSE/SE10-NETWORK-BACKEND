package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.InspireDTO;
import lk.ijse.SE10_NETWORK_BACKEND.service.InspireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/inspire")
@CrossOrigin("*")
public class InspireController {

    @Autowired
    private InspireService inspireService;

    private static final Logger logger = LoggerFactory.getLogger(InspireController.class);

    /**
     * Saves a "like" (inspiration) for a post.
     *
     * @param dto   The data transfer object representing the inspiration.
     * @param token The JWT token for find user.
     * @return A ResponseEntity with appropriate HTTP status.
     */
    @PostMapping
    public ResponseEntity<String> saveLike(
            @RequestBody InspireDTO dto,
            @RequestHeader("Authorization") String token) {

        logger.info("Attempting to save inspiration for post: {}", dto.getPostId());

        InspireDTO inspireDTO = inspireService.saveInspiration(dto, token);

        if (inspireDTO != null) {
            logger.info("Inspiration saved successfully for post: {}", dto.getPostId());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            logger.warn("Failed to save inspiration for post: {}", dto.getPostId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Deletes a "like" (inspiration) for a post.
     *
     * @param postId The ID of the post to delete the inspiration from.
     * @param token  The JWT token for find user.
     * @return A ResponseEntity with appropriate HTTP status.
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteLike(
            @PathVariable("postId") Long postId,
            @RequestHeader("Authorization") String token) {

        logger.info("Attempting to delete inspiration for post: {}", postId);

        boolean deleted = inspireService.deleteInspiration(postId, token);

        if (deleted) {
            logger.info("Inspiration deleted successfully for post: {}", postId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            logger.warn("Failed to delete inspiration for post: {}", postId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
