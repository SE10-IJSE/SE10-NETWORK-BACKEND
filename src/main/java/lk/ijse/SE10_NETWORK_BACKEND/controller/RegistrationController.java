package lk.ijse.SE10_NETWORK_BACKEND.controller;

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
}
