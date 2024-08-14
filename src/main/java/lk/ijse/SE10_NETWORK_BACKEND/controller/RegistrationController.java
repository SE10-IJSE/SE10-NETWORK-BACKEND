package lk.ijse.SE10_NETWORK_BACKEND.controller;
import lk.ijse.SE10_NETWORK_BACKEND.dto.SignInDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    @Autowired
    private UserService userService;
    @PostMapping("/sign_in")
    public ResponseEntity<String> signIn(@RequestBody SignInDTO dto) {
        UserDTO userDTO = userService.configureUser(dto);

        if (userDTO != null) {
            return ResponseEntity.status(HttpStatus.OK).body("User sign in Successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User sign in failed");
        }
    }

    @PostMapping("/sign_up")
    public ResponseEntity<String> signUP(@RequestBody UserDTO dto) {
        UserDTO userDTO = userService.saveUser(dto);

        if (userDTO != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Sign Up Successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sign Up Failed");
        }
    }
}
