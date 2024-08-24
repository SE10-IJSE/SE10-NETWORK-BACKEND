package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.AuthDTO;
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
        System.out.println(userDTO.getNewPassword());
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
                            .body(new ResponseDTO(VarList.OK, "Success", authDTO));
                }
                case VarList.Not_Acceptable -> {
                    logger.warn("User update failed for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                            .body(new ResponseDTO(VarList.Not_Acceptable, "Password does not match", null));
                }
                case VarList.Not_Found -> {
                    logger.warn("User update failed for id: {}", userDTO.getUserId());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ResponseDTO(VarList.Not_Found, "User does not exist", null));
                }
                default -> {
                    logger.error("Error occurred during update for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(new ResponseDTO(VarList.Bad_Gateway, "Error", null));
                }
            }
        } catch (Exception e) {
            logger.error("Update failed for email: {}", userDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
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
     * Get a user by ID.
     *
     * @param token The token of the user to retrieve.
     * @return ResponseEntity containing the UserDTO if found, or a not found status.
     */
    @GetMapping
    public ResponseEntity<UserDTO> getUserByEmail(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.getUsernameFromToken(token.substring(7));
        System.out.println(email);
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
