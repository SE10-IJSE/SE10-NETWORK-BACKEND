package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.InspireDTO;
import lk.ijse.SE10_NETWORK_BACKEND.service.InspireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inspire")
public class InspireController {
    @Autowired
    private InspireService inspireService;

    @PostMapping("/save")
    public ResponseEntity<String> saveLike(@RequestBody InspireDTO dto) {
        InspireDTO inspireDTO = inspireService.saveInspiration(dto);

        if (inspireDTO != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Inspire saved successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Inspiration not saved");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLike(@PathVariable Long id) {
        boolean deleted = inspireService.deleteInspiration(id);

        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
