package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody UserDTO dto) {
        return ResponseEntity.ok("User updated successfully.");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get_data/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable("id") Long id) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(id);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/birthdays")
    public ResponseEntity<List<UserDTO>> getUsersWithBirthday() {
        List<UserDTO> list = new ArrayList<UserDTO>();
        return ResponseEntity.ok(list);
    }
}