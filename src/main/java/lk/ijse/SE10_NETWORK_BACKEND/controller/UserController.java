package lk.ijse.SE10_NETWORK_BACKEND.controller;

import jakarta.validation.Valid;
import lk.ijse.SE10_NETWORK_BACKEND.dto.*;
import lk.ijse.SE10_NETWORK_BACKEND.exception.DataPersistFailedException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.InvalidPasswordException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserEmailMismatchException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserNotFoundException;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.VarList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    /**
     * Updates an existing user with the provided details.
     *
     * @param userDTO The data transfer object containing updated user information.
     * @return ResponseEntity with status 200 (OK) and an AuthDTO on success, 406 (Not Acceptable) on email or password mismatch, 404 (Not Found) if the user is not found, and 500 (Internal Server Error) for other errors.
     */
    @PutMapping
    public ResponseEntity<ResponseDTO> updateUser(
            @Valid @RequestBody UserDTO userDTO,
            @RequestHeader("Authorization") String jwt) {
        log.info("Attempting to update user with email: {}", userDTO.getEmail());
        try {
            userService.updateUser(userDTO, jwt.substring(7));
            String token = jwtUtil.generateToken(userDTO);
            AuthDTO authDTO = new AuthDTO();
            authDTO.setEmail(userDTO.getEmail());
            authDTO.setToken(token);
            log.info("User successfully updated with email: {}", userDTO.getEmail());
            return ResponseEntity.ok(new ResponseDTO(VarList.OK, "User updated successfully", authDTO));
        } catch (UserEmailMismatchException e) {
            log.warn("Update failed: Email mismatch for email: {}", userDTO.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ResponseDTO(VarList.Not_Acceptable, "Email does not match", null));
        } catch (UserNotFoundException e) {
            log.warn("User not found with email: {}", userDTO.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(VarList.Not_Found, "User does not exist", null));
        } catch (InvalidPasswordException e) {
            log.warn("Update failed: Password mismatch for email: {}", userDTO.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ResponseDTO(VarList.Not_Acceptable, "Password does not match", null));
        } catch (Exception e) {
            log.error("Update failed for email: {}", userDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, "Internal server error", null));
        }
    }
    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to be deleted.
     * @return ResponseEntity with status 204 (No Content) on success, 406 (Not Acceptable) on email mismatch, 404 (Not Found) if the user is not found, and 500 (Internal Server Error) for other errors.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String jwt) {
        try {
            log.info("Request to delete user with ID: {}", id);
            userService.deleteUser(id, jwt.substring(7));
            log.info("User with ID: {} deleted successfully", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UserEmailMismatchException e) {
            log.warn("Email mismatch for user with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Email mismatch");
        } catch (UserNotFoundException e) {
            log.warn("User with ID: {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception e) {
            log.error("Failed to delete user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete user");
        }
    }
    /**
     * Retrieves a user by email using the JWT token provided in the Authorization header.
     *
     * @param token The JWT token from the Authorization header.
     * @return ResponseEntity with status 200 (OK) and the UserDTO on success, and 404 (Not Found) if the user is not found.
     */
    @GetMapping
    public ResponseEntity<UserDTO> getUserByToken(@RequestHeader("Authorization") String token) {
        log.info("Retrieving user information using JWT token");
        String email = jwtUtil.getUsernameFromToken(token.substring(7));
        UserDTO dto = userService.getUserByEmail(email);
        if (dto != null) {
            log.info("User with email: {} retrieved successfully", email);
            return ResponseEntity.ok(dto);
        } else {
            log.warn("User with email: {} not found", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    /**
     * Retrieves a user by email.
     *
     * @param email The email of the user.
     * @return ResponseEntity with status 200 (OK) and the UserDTO on success, and 404 (Not Found) if the user is not found.
     */
    @GetMapping("/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable("email") String email) {
        log.info("Retrieving user information using User name");
        UserDTO dto = userService.getUserByUserEmail(email);
        if (dto != null) {
            log.info("User with email: {} retrieved successfully", email);
            return ResponseEntity.ok(dto);
        } else {
            log.warn("User with email: {} not found", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    /**
     * Searches for users by name with pagination.
     *
     * @param name   The name or partial name to search for.
     * @param pageNo The page number for pagination.
     * @return ResponseEntity with status 200 (OK) and a list of UserSearchDTO objects on success, and 404 (Not Found) if no users are found.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDTO>> searchUser(
            @RequestParam("name") String name, @RequestParam("pageNo") int pageNo) {
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
     * @return ResponseEntity with status 200 (OK) and the profile image URL on success, and 404 (Not Found) if the profile image is not found.
     */
    @GetMapping("/profileImg")
    public ResponseEntity<String> getProfileImg(@RequestHeader("Authorization") String token) {
        log.info("Received request to get profile image with Authorization token.");
        String profileImg = userService.getProfileImg(token.substring(7));
        if (profileImg.isEmpty()) {
            log.warn("Profile image not found for token.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            log.info("Profile image found and returned successfully for token.");
            return ResponseEntity.status(HttpStatus.OK).body(profileImg);
        }
    }
    /**
     * Updates the user's image (profile or cover).
     *
     * @param dto The data transfer object containing image update details.
     * @return ResponseEntity with status 200 (OK) and a boolean indicating success, 404 (Not Found) if the user is not found, and 500 (Internal Server Error) for other errors.
     */
    @PutMapping("/image")
    public ResponseEntity<Boolean> updateUserImage(
            @Valid @ModelAttribute ImageUpdateDTO dto,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.getUsernameFromToken(token.substring(7));
        try {
            log.info("Updating {} image for user with email: {}", dto.getType(), email);
            userService.updateUserImage(dto, token.substring(7));
            log.info("{} image updated successfully for email: {}", dto.getType(), email);
            return ResponseEntity.ok(true);
        } catch (UserNotFoundException e) {
            log.warn("User with email: {} not found", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (DataPersistFailedException e) {
            log.error("Failed to update {} image for email: {}", dto.getType(), email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    /**
     * Deletes the user's image (profile or cover).
     *
     * @param dto The data transfer object containing image delete details.
     * @return ResponseEntity with status 200 (OK) and a boolean indicating success, 404 (Not Found) if the user is not found, and 500 (Internal Server Error) for other errors.
     */
    @DeleteMapping("/image")
    public ResponseEntity<Boolean> deleteUserImage(
            @Valid @RequestBody ImageUpdateDTO dto,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.getUsernameFromToken(token.substring(7));
        try {
            log.info("Deleting {} image for user with email: {}", dto.getType(), email);
            userService.deleteUserImage(dto, token.substring(7));
            log.info("{} image deleted successfully for email: {}", dto.getType(), email);
            return ResponseEntity.ok(true);
        } catch (UserNotFoundException e) {
            log.warn("User with email: {} not found", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (DataPersistFailedException e) {
            log.error("Failed to update {} image for email: {}", dto.getType(), email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    /**
     * Retrieves names of users with birthdays today.
     *
     * @return ResponseEntity with status 200 (OK) and a list of names on success, and 404 (Not Found) if no users are found.
     */
    @GetMapping("/birthday/names")
    public ResponseEntity<List<String>> getUsernamesWithBirthdays() {
        log.info("Fetching names of users with birthdays today");
        List<String> usersList = userService.getUserNamesWithBirthdaysToday();
        if (usersList != null && !usersList.isEmpty()) {
            log.info("Found {} users with birthdays today", usersList.size());
            return ResponseEntity.ok(usersList);
        } else {
            log.warn("No users with birthdays today");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    /**
     * Retrieves detailed information of users with birthdays today.
     *
     * @return ResponseEntity with status 200 (OK) and a list of UserSearchDTO objects on success, and 404 (Not Found) if no users are found.
     */
    @GetMapping("/birthday/data")
    public ResponseEntity<List<UserSearchDTO>> getUsersWithBirthdays() {
        log.info("Fetching detailed information for users with birthdays today");
        List<UserSearchDTO> list = userService.getUsersWithBirthdaysToday();
        if (list != null && !list.isEmpty()) {
            log.info("Found {} users with birthdays today", list.size());
            return ResponseEntity.ok(list);
        } else {
            log.warn("No users with birthdays today");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    /**
     * Validates the JWT token received in the request header.
     *
     * @param token The JWT token extracted from the Authorization header, prefixed with "Bearer ".
     * @return ResponseEntity with status 200 (OK) and a boolean indicating the token is valid.
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateUser(@RequestHeader("Authorization") String token) {
        log.info("Validating JWT token");
        String jwtToken = token.substring(7); // Remove "Bearer " prefix
        String username = jwtUtil.getUsernameFromToken(jwtToken);
        log.debug("Extracted username from token: {}", username);
        return ResponseEntity.ok(true);
    }
}