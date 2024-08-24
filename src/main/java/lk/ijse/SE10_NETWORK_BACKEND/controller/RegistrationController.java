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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    /**
     * Constructor for RegistrationController.
     *
     * @param authenticationManager The authentication manager for handling authentication.
     * @param jwtUtil The utility class for handling JWT operations.
     * @param userDetailsService The service for loading user details.
     */
    public RegistrationController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Handle user sign-in.
     *
     * @param dto The SignInDTO containing email and password for authentication.
     * @return ResponseEntity with a ResponseDTO indicating the result of the sign-in attempt.
     */
    @PostMapping("/sign_in")
    public ResponseEntity<ResponseDTO> signIn(@RequestBody SignInDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        } catch (Exception e) {
            logger.error("Authentication failed for email: {}", dto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO(VarList.Unauthorized, "Invalid Credentials", e.getMessage()));
        }

        UserDTO loadedUser = userService.loadUserDetailsByEmail(dto.getEmail());
        if (loadedUser == null) {
            logger.warn("User with email: {} not found", dto.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseDTO(VarList.Conflict, "Authorization Failure! Please Try Again", null));
        }

        String token = jwtUtil.generateToken(loadedUser);
        if (token == null || token.isEmpty()) {
            logger.warn("Failed to generate token for email: {}", dto.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseDTO(VarList.Conflict, "Authorization Failure! Please Try Again", null));
        }

        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail(loadedUser.getEmail());
        authDTO.setToken(token);
        logger.info("User signed in successfully with email: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDTO(VarList.Created, "Sign In Successfully", authDTO));
    }

    /**
     * Handle user sign-up.
     *
     * @param userDTO The UserDTO containing user details for registration.
     * @return ResponseEntity with a ResponseDTO indicating the result of the sign-up attempt.
     */
    @PostMapping("/sign_up")
    public ResponseEntity<ResponseDTO> signUp(@RequestBody UserDTO userDTO) {
        try {
            int res = userService.saveUser(userDTO);
            switch (res) {
                case VarList.Created -> {
                    String token = jwtUtil.generateToken(userDTO);
                    AuthDTO authDTO = new AuthDTO();
                    authDTO.setEmail(userDTO.getEmail());
                    authDTO.setToken(token);
                    logger.info("User signed up successfully with email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(new ResponseDTO(VarList.Created, "Success", authDTO));
                }
                case VarList.Not_Acceptable -> {
                    logger.warn("Email already used for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                            .body(new ResponseDTO(VarList.Not_Acceptable, "Email Already Used", null));
                }
                default -> {
                    logger.error("Error occurred during sign-up for email: {}", userDTO.getEmail());
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(new ResponseDTO(VarList.Bad_Gateway, "Error", null));
                }
            }
        } catch (Exception e) {
            logger.error("Sign-up failed for email: {}", userDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    /**
     * Validate a JWT token.
     *
     * @param token The JWT token to be validated, provided in the Authorization header.
     * @return ResponseEntity indicating whether the token is valid.
     */
    @GetMapping("/validate")
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
