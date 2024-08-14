package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
public class PostController {
    @PostMapping("/save")
    public ResponseEntity<String> savePost(@RequestBody PostDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Post saved successfully");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updatePost(@RequestBody PostDTO dto) {
        return ResponseEntity.ok().body("Post updated successfully");
    }

    @PutMapping("/{postId}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable("postId") Long postId,
            @RequestParam("adminId") Long adminId) {
        return ResponseEntity.ok().body("Post id: " + postId + " approved by admin id: " + adminId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id) {
        PostDTO dto = new PostDTO();
        dto.setPostId(id);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPost(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount) {
        List<PostDTO> dtos = new ArrayList<>();
        return ResponseEntity.ok().body(dtos);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<PostDTO>> getPostsByStudentId(
            @PathVariable("id") Long userId,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount) {
        List<PostDTO> dtos = new ArrayList<>();
        return ResponseEntity.ok().body(dtos);
    }

    @GetMapping("/unapproved")
    public ResponseEntity<List<PostDTO>> getUnapprovedPosts(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount) {
        List<PostDTO> dtos = new ArrayList<>();
        return ResponseEntity.ok().body(dtos);
    }
}