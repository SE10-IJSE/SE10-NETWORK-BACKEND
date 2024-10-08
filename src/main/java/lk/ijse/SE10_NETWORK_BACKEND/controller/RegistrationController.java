package lk.ijse.SE10_NETWORK_BACKEND.controller;

import jakarta.mail.MessagingException;
import lk.ijse.SE10_NETWORK_BACKEND.customObj.OtpResponse;
import lk.ijse.SE10_NETWORK_BACKEND.dto.AuthDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.ResponseDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.SignInDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.VarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
public class RegistrationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    /**
     * Handles user sign-in requests.
     *
     * @param dto SignInDTO containing the email and password for authentication.
     * @return ResponseEntity with a ResponseDTO indicating the result of the sign-in attempt.
     */
    @PostMapping("/sign_in")
    public ResponseEntity<ResponseDTO> signIn(@RequestBody SignInDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        } catch (Exception e) {
            logger.error("Sign-in failed for email: {}. Error: {}", dto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO(VarList.Unauthorized, "Invalid Credentials", e.getMessage()));
        }

        UserDTO loadedUser = userService.loadUserDetailsByEmail(dto.getEmail());
        if (loadedUser == null) {
            logger.warn("User not found for email: {}", dto.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(VarList.Conflict, "User not found", null));
        }

        String token = jwtUtil.generateToken(loadedUser);
        if (token == null || token.isEmpty()) {
            logger.warn("Failed to generate token for email: {}", dto.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Conflict, "Token generation failed", null));
        }

        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail(loadedUser.getEmail());
        authDTO.setToken(token);
        logger.info("Sign-in successful for email: {}", dto.getEmail());
        return ResponseEntity.ok(new ResponseDTO(VarList.Created, "Sign-in successful", authDTO));
    }

    /**
     * Handles user sign-up requests.
     *
     * @param userDTO UserDTO containing the user details for registration.
     * @return ResponseEntity with a ResponseDTO indicating the result of the sign-up attempt.
     */
    @PostMapping("/sign_up")
    public ResponseEntity<ResponseDTO> signUp(@ModelAttribute UserDTO userDTO) {
        try {
            int result = userService.saveUser(userDTO);
            switch (result) {
                case VarList.Created -> {
                    String token = jwtUtil.generateToken(userDTO);
                    AuthDTO authDTO = new AuthDTO();
                    authDTO.setEmail(userDTO.getEmail());
                    authDTO.setToken(token);
                    logger.info("Sign-up successful for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(new ResponseDTO(VarList.Created, "User created successfully", authDTO));
                }
                case VarList.Not_Acceptable -> {
                    logger.warn("Email already in use for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new ResponseDTO(VarList.Not_Acceptable, "Email already in use", null));
                }
                default -> {
                    logger.error("Sign-up error for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(new ResponseDTO(VarList.Bad_Gateway, "Sign-up failed", null));
                }
            }
        } catch (Exception e) {
            logger.error("Sign-up exception for email: {}. Error: {}", userDTO.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, "Sign-up failed", null));
        }
    }

    /**
     * Verifies the email address of a user.
     *
     * @param name  The name of the user whose email is to be verified.
     * @param email The email address to be verified.
     * @return ResponseEntity with an OtpResponse indicating the result of the email verification attempt.
     */
    @PostMapping("/request_otp")
    public ResponseEntity<OtpResponse> verifyEmail(
            @RequestParam("name") String name,
            @RequestParam("email") String email) {
        try {
            logger.info("Starting email verification process for user: {}, email: {}", name, email);

            userService.verifyUserEmail(name, email);

            logger.info("Email verification successful for user: {}, email: {}", name, email);
            return ResponseEntity.ok().build();
        } catch (MessagingException | IOException e) {
            logger.error("Error during email verification for user: {}, email: {}. Error: {}", name, email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verifies the OTP sent to the user's email address.
     *
     * @param email The email address of the user.
     * @param otp   The OTP to be verified.
     * @return ResponseEntity with a ResponseDTO indicating the result of the OTP verification attempt.
     */
    @GetMapping("/verify_otp")
    public ResponseEntity<Void> verifyOtp(
            @RequestParam("email") String email,
            @RequestParam("otp") String otp) {
        try {
            boolean result = userService.verifyOtp(email, otp);
            if (result) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            logger.error("Error during OTP verification. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the password for a user identified by their email.
     *
     * @param email    The email of the user whose password is to be updated.
     * @param password The new password to be set.
     * @return ResponseEntity indicating the success or failure of the password update attempt.
     */
    @PutMapping("/update_password")
    public ResponseEntity<Void> updatePassword(
            @RequestParam("email") String email,
            @RequestParam("password") String password) {
        try {
            logger.info("Starting password update for email: {}", email);

            userService.updatePassword(email, password);

            logger.info("Password updated successfully for email: {}", email);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error updating password for email: {} , Error: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
