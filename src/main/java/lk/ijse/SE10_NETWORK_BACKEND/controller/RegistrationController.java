package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.SignInDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    @PostMapping("/sign_in")
    public ResponseEntity<String> signIn(@RequestBody SignInDTO dto) {
        return ResponseEntity.ok("Sign In Successfully");
    }

    @PostMapping("/sign_up")
    public ResponseEntity<String> signUP(@RequestBody UserDTO dto) {
        return ResponseEntity.ok("Sign Up Successfully");
    }
}