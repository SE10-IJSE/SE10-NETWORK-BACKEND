package lk.ijse.SE10_NETWORK_BACKEND.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lk.ijse.SE10_NETWORK_BACKEND.customObj.OtpResponse;
import lk.ijse.SE10_NETWORK_BACKEND.dto.*;
import lk.ijse.SE10_NETWORK_BACKEND.exception.DataPersistFailedException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.InvalidOtpException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserEmailMismatchException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserNotFoundException;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.VarList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    /**
     * Handles user sign-in requests.
     *
     * @param dto SignInDTO containing the email and password for authentication.
     * @return ResponseEntity with status 200 (OK) and an AuthDTO on success, 401 (Unauthorized) on invalid credentials, and 404 (Not Found) if the user is not found.
     */
    @PostMapping("/sign_in")
    public ResponseEntity<ResponseDTO> signIn(@Valid @RequestBody SignInDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        } catch (Exception e) {
            log.error("Sign-in failed for email: {}. Error: {}", dto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO(VarList.Unauthorized, "Invalid Credentials", e.getMessage()));
        }
        UserDTO loadedUser = userService.loadUserDetailsByEmail(dto.getEmail());
        if (loadedUser == null) {
            log.warn("User not found for email: {}", dto.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(VarList.Not_Found, "User not found", null));
        }
        String token = jwtUtil.generateToken(loadedUser);
        if (token == null || token.isEmpty()) {
            log.warn("Failed to generate token for email: {}", dto.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, "Token generation failed", null));
        }
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail(loadedUser.getEmail());
        authDTO.setToken(token);
        log.info("Sign-in successful for email: {}", dto.getEmail());
        return ResponseEntity.ok(new ResponseDTO(VarList.Created, "Sign-in successful", authDTO));
    }
    /**
     * Handles user sign-up requests.
     *
     * @param userDTO UserDTO containing the user details for registration.
     * @return ResponseEntity with status 201 (Created) and an AuthDTO on success, 400 (Bad Request) on invalid OTP, 409 (Conflict) on email mismatch, and 500 (Internal Server Error) on data persistence failure.
     */
    @PostMapping("/sign_up")
    public ResponseEntity<ResponseDTO> signUp(@Valid @ModelAttribute SignUpDTO userDTO) {
        try {
            userService.saveUser(userDTO);
            String token = jwtUtil.generateToken(modelMapper.map(userDTO, UserDTO.class));
            AuthDTO authDTO = new AuthDTO();
            authDTO.setEmail(userDTO.getEmail());
            authDTO.setToken(token);
            log.info("Sign-up successful for email: {}", userDTO.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO(VarList.Created, "User created successfully", authDTO));
        } catch (InvalidOtpException e) {
            log.warn("Invalid OTP for email: {}", userDTO.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(VarList.Bad_Request, "Invalid OTP", null));
        } catch (UserEmailMismatchException e) {
            log.warn("User and email data are inconsistent.");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseDTO(VarList.Conflict, "Email mismatch", null));
        } catch (DataPersistFailedException e) {
            log.error("Error during user registration. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, "User registration failed", null));
        }
    }
    /**
     * Updates the password for a user identified by their email.
     * This method verifies the provided OTP and updates the user's password if the OTP is valid.
     *
     * @param dto UpdatePasswordDTO containing the email, OTP, and new password.
     * @return ResponseEntity with status 204 (No Content) on success, 400 (Bad Request) on invalid OTP, and 404 (Not Found) if the user is not found.
     */
    @PutMapping("/update_password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        try {
            log.info("Starting password update for email: {}", dto.getEmail());
            userService.updatePassword(dto);
            log.info("Password updated successfully for email: {}", dto.getEmail());
            return ResponseEntity.noContent().build();
        } catch (InvalidOtpException e) {
            log.warn("Invalid OTP for email: {}", dto.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (UserNotFoundException e) {
            log.warn("User not found for email: {}", dto.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error updating password for email: {} , Error: {}", dto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /**
     * Verifies the email address of a user.
     *
     * @param name  The name of the user whose email is to be verified.
     * @param email The email address to be verified.
     * @return ResponseEntity with status 200 (OK) on success, 404 (Not Found) if the user is not found, and 500 (Internal Server Error) on messaging or IO exceptions.
     */
    @PostMapping("/request_otp")
    public ResponseEntity<OtpResponse> requestOtp(
            @RequestParam("name") String name,
            @RequestParam("email") String email) {
        try {
            log.info("Starting email verification process for user: {}, email: {}", name, email);
            userService.verifyUserEmail(name, email);
            log.info("Email verification successful for user: {}, email: {}", name, email);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            log.warn("User not found for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (MessagingException | IOException e) {
            log.error("Error during email verification for user: {}, email: {}. Error: {}", name, email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}