package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.InspireDTO;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inspire")
public class InspireController {
    @PostMapping("/save")
    public ResponseEntity<String> saveLike(@RequestBody InspireDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Liked post successfully");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteLike(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}