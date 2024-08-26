package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.AuthDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.ImageUpdateDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.ResponseDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.VarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Update an existing user.
     *
     * @param userDTO The user data transfer object containing the updated information.
     * @return ResponseEntity with a message indicating the outcome of the update operation.
     */
    @PutMapping
    public ResponseEntity<ResponseDTO> updateUser(@RequestBody UserDTO userDTO) {
        logger.info("Received request to update user with email: {}", userDTO.getEmail());

        try {
            int res = userService.updateUser(userDTO);
            switch (res) {
                case VarList.OK -> {
                    String token = jwtUtil.generateToken(userDTO);
                    AuthDTO authDTO = new AuthDTO();
                    authDTO.setEmail(userDTO.getEmail());
                    authDTO.setToken(token);
                    logger.info("User updated successfully with email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(new ResponseDTO(VarList.OK, "User updated successfully", authDTO));
                }
                case VarList.Not_Acceptable -> {
                    logger.warn("User update failed due to password mismatch for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                            .body(new ResponseDTO(VarList.Not_Acceptable, "Password does not match", null));
                }
                case VarList.Not_Found -> {
                    logger.warn("User not found with ID: {}", userDTO.getUserId());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ResponseDTO(VarList.Not_Found, "User does not exist", null));
                }
                default -> {
                    logger.error("An unexpected error occurred during update for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(new ResponseDTO(VarList.Bad_Gateway, "Error occurred", null));
                }
            }
        } catch (Exception e) {
            logger.error("Update failed for email: {}", userDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, "Internal server error occurred", null));
        }
    }

    /**
     * Update a user's image (profile or cover).
     *
     * @param dto The data transfer object containing the image update information.
     * @return ResponseEntity indicating whether the image update was successful.
     */
    @PutMapping("/image")
    public ResponseEntity<Boolean> updateUserImage(@ModelAttribute ImageUpdateDTO dto) {
        logger.info("Received request to update {} image for user with email: {}", dto.getType(), dto.getEmail());

        boolean updated = userService.updateUserImage(dto);
        if (updated) {
            logger.info("User {} image updated successfully for email: {}", dto.getType(), dto.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } else {
            logger.warn("Failed to update {} image for user with email: {}", dto.getType(), dto.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    /**
     * Delete a user's image (profile or cover).
     *
     * @param dto The data transfer object containing the image delete information.
     * @return ResponseEntity indicating whether the image deletion was successful.
     */
    @DeleteMapping("/image")
    public ResponseEntity<Boolean> deleteUserImage(@RequestBody ImageUpdateDTO dto) {
        logger.info("Received request to delete {} image for user with email: {}", dto.getType(), dto.getEmail());

        boolean deleted = userService.deleteUserImage(dto);
        if (deleted) {
            logger.info("User {} image deleted successfully for email: {}", dto.getType(), dto.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } else {
            logger.warn("Failed to delete {} image for user with email: {}", dto.getType(), dto.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    /**
     * Delete a user by ID.
     *
     * @param id The ID of the user to be deleted.
     * @return ResponseEntity indicating the outcome of the delete operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
        logger.info("Received request to delete user with ID: {}", id);

        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            logger.info("User with ID: {} deleted successfully", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            logger.warn("User with ID: {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    /**
     * Get a user by email, extracted from the JWT token.
     *
     * @param token The JWT token from the Authorization header.
     * @return ResponseEntity containing the UserDTO if found, or a not found status.
     */
    @GetMapping
    public ResponseEntity<UserDTO> getUserByEmail(@RequestHeader("Authorization") String token) {
        logger.info("Received request to retrieve user by JWT token");

        String email = jwtUtil.getUsernameFromToken(token.substring(7));
        UserDTO dto = userService.getUserByEmail(email);

        if (dto != null) {
            logger.info("User with email: {} retrieved successfully", email);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } else {
            logger.warn("User with email: {} not found", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Get users whose birthdays are today.
     *
     * @return ResponseEntity containing a list of UserDTOs with birthdays today, or a not found status.
     */
    @GetMapping("/birthdays")
    public ResponseEntity<List<UserDTO>> getUsersWithBirthdaysToday() {
        logger.info("Received request to retrieve users with birthdays today");

        List<UserDTO> usersWithBirthdaysToday = userService.getUsersWithBirthdaysToday();
        if (usersWithBirthdaysToday != null && !usersWithBirthdaysToday.isEmpty()) {
            logger.info("Found {} users with birthdays today", usersWithBirthdaysToday.size());
            return ResponseEntity.status(HttpStatus.OK).body(usersWithBirthdaysToday);
        } else {
            logger.warn("No users found with birthdays today");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Validate a JWT token.
     *
     * @param token The JWT token to be validated, provided in the Authorization header.
     * @return ResponseEntity indicating whether the token is valid.
     */
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateUser(@RequestHeader("Authorization") String token) {
        logger.info("Received request to validate JWT token");

        String jwtToken = token.substring(7); // Remove "Bearer " prefix
        String username = jwtUtil.getUsernameFromToken(jwtToken);

        logger.debug("Extracted username from token: {}", username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        boolean isValid = jwtUtil.validateToken(jwtToken, userDetails);

        if (isValid) {
            logger.info("JWT token is valid for username: {}", username);
        } else {
            logger.warn("JWT token is invalid for username: {}", username);
        }
        return ResponseEntity.status(HttpStatus.OK).body(isValid);
    }
}
