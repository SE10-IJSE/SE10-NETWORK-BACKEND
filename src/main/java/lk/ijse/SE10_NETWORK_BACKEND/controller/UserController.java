package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.*;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.VarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Updates an existing user with the provided details.
     *
     * @param userDTO The data transfer object containing updated user information.
     * @return ResponseEntity with status and message indicating the result of the update operation.
     */
    @PutMapping
    public ResponseEntity<ResponseDTO> updateUser(@RequestBody UserDTO userDTO) {
        logger.info("Attempting to update user with email: {}", userDTO.getEmail());

        try {
            int result = userService.updateUser(userDTO);
            switch (result) {
                case VarList.OK -> {
                    String token = jwtUtil.generateToken(userDTO);
                    AuthDTO authDTO = new AuthDTO();
                    authDTO.setEmail(userDTO.getEmail());
                    authDTO.setToken(token);
                    logger.info("User successfully updated with email: {}", userDTO.getEmail());
                    return ResponseEntity.ok(new ResponseDTO(VarList.OK, "User updated successfully", authDTO));
                }
                case VarList.Not_Acceptable -> {
                    logger.warn("Update failed: Password mismatch for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                            .body(new ResponseDTO(VarList.Not_Acceptable, "Password does not match", null));
                }
                case VarList.Not_Found -> {
                    logger.warn("User not found with ID: {}", userDTO.getUserId());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ResponseDTO(VarList.Not_Found, "User does not exist", null));
                }
                default -> {
                    logger.error("Unexpected error during update for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(new ResponseDTO(VarList.Bad_Gateway, "An error occurred", null));
                }
            }
        } catch (Exception e) {
            logger.error("Update failed for email: {}", userDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, "Internal server error", null));
        }
    }

    /**
     * Updates the user's image (profile or cover).
     *
     * @param dto The data transfer object containing image update details.
     * @return ResponseEntity indicating whether the image update was successful.
     */
    @PutMapping("/image")
    public ResponseEntity<Boolean> updateUserImage(@ModelAttribute ImageUpdateDTO dto) {
        logger.info("Updating {} image for user with email: {}", dto.getType(), dto.getEmail());

        boolean updated = userService.updateUserImage(dto);
        if (updated) {
            logger.info("{} image updated successfully for email: {}", dto.getType(), dto.getEmail());
            return ResponseEntity.ok(true);
        } else {
            logger.warn("Failed to update {} image for email: {}", dto.getType(), dto.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    /**
     * Deletes the user's image (profile or cover).
     *
     * @param dto The data transfer object containing image delete details.
     * @return ResponseEntity indicating whether the image deletion was successful.
     */
    @DeleteMapping("/image")
    public ResponseEntity<Boolean> deleteUserImage(@RequestBody ImageUpdateDTO dto) {
        logger.info("Deleting {} image for user with email: {}", dto.getType(), dto.getEmail());

        boolean deleted = userService.deleteUserImage(dto);
        if (deleted) {
            logger.info("{} image deleted successfully for email: {}", dto.getType(), dto.getEmail());
            return ResponseEntity.ok(true);
        } else {
            logger.warn("Failed to delete {} image for email: {}", dto.getType(), dto.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to be deleted.
     * @return ResponseEntity indicating the result of the delete operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
        logger.info("Request to delete user with ID: {}", id);

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
     * Retrieves a user by email using the JWT token provided in the Authorization header.
     *
     * @param token The JWT token from the Authorization header.
     * @return ResponseEntity containing the UserDTO if found, or a NOT FOUND status.
     */
    @GetMapping
    public ResponseEntity<UserDTO> getUserByEmail(@RequestHeader("Authorization") String token) {
        logger.info("Retrieving user information using JWT token");

        String email = jwtUtil.getUsernameFromToken(token.substring(7));
        UserDTO dto = userService.getUserByEmail(email);

        if (dto != null) {
            logger.info("User with email: {} retrieved successfully", email);
            return ResponseEntity.ok(dto);
        } else {
            logger.warn("User with email: {} not found", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Retrieves names of users with birthdays today.
     *
     * @return ResponseEntity containing a list of names if users are found; otherwise, a NOT FOUND status.
     */
    @GetMapping("/birthday/names")
    public ResponseEntity<List<String>> getUserNamesWithBirthdays() {
        logger.info("Fetching names of users with birthdays today");

        List<String> usersList = userService.getUserNamesWithBirthdaysToday();

        if (usersList != null && !usersList.isEmpty()) {
            logger.info("Found {} users with birthdays today", usersList.size());
            return ResponseEntity.ok(usersList);
        } else {
            logger.warn("No users with birthdays today");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Retrieves detailed information of users with birthdays today.
     *
     * @return ResponseEntity containing a list of UserSearchDTO objects if users are found; otherwise, a NOT FOUND status.
     */
    @GetMapping("/birthday/data")
    public ResponseEntity<List<UserSearchDTO>> getUsersWithBirthdays() {
        logger.info("Fetching detailed information for users with birthdays today");

        List<UserSearchDTO> list = userService.getUsersWithBirthdaysToday();

        if (list != null && !list.isEmpty()) {
            logger.info("Found {} users with birthdays today", list.size());
            return ResponseEntity.ok(list);
        } else {
            logger.warn("No users with birthdays today");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Searches for users by name with pagination.
     *
     * @param name The name or partial name to search for.
     * @param pageNo The page number for pagination.
     * @return ResponseEntity containing a list of UserSearchDTO objects if users are found; otherwise, a NOT FOUND status.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDTO>> searchUser(@RequestParam("name") String name, @RequestParam("pageNo") int pageNo) {
        List<UserSearchDTO> users = userService.findUsersByNameOrNameLike(name, pageNo);

        if (users != null && !users.isEmpty()) {
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Retrieves the profile image of the authenticated user based on the provided JWT token.
     *
     * @param token The JWT token from the Authorization header (with "Bearer " prefix).
     * @return ResponseEntity with the profile image URL and HTTP status 200 (OK).
     */
    @GetMapping("/profileImg")
    public ResponseEntity<String> getProfileImg(@RequestHeader("Authorization") String token) {
        String profileImgUrl = userService.getProfileImg(token.substring(7));
        return ResponseEntity.status(HttpStatus.OK).body(profileImgUrl);
    }

    /**
     * Validates the JWT token received in the request header.
     *
     * @param token The JWT token extracted from the Authorization header, prefixed with "Bearer ".
     * @return ResponseEntity with status 200 OK and a body of true, indicating that the token is valid.
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateUser(@RequestHeader("Authorization") String token) {
        logger.info("Validating JWT token");

        String jwtToken = token.substring(7); // Remove "Bearer " prefix
        String username = jwtUtil.getUsernameFromToken(jwtToken);
        logger.debug("Extracted username from token: {}", username);

        return ResponseEntity.ok(true);
    }
}
