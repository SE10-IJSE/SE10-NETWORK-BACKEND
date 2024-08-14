package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import lk.ijse.SE10_NETWORK_BACKEND.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping("/save")
    public ResponseEntity<String> savePost(@RequestBody PostDTO dto) {
        PostDTO postDTO = postService.savePost(dto);

        if (postDTO != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Post saved successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post could not be saved");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updatePost(@RequestBody PostDTO dto) {
        PostDTO postDTO = postService.updatePost(dto);

        if (postDTO != null) {
            return ResponseEntity.status(HttpStatus.OK).body("Post updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post could not be updated");
        }
    }

    @PutMapping("/{postId}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable("postId") Long postId,
            @RequestParam("adminId") Long adminId) {
        PostDTO postDTO = postService.updatePostStatus(postId, adminId);

        if (postDTO != null) {
            return ResponseEntity.status(HttpStatus.OK).body("Post status updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post could not be updated");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id) {
        PostDTO postById = postService.getPostById(id);

        if (postById != null) {
            return ResponseEntity.status(HttpStatus.OK).body(postById);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPost(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount) {
        List<PostDTO> allPosts = postService.getAllPosts(pageNo, postCount);

        if (allPosts != null) {
            return ResponseEntity.status(HttpStatus.OK).body(allPosts);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<PostDTO>> getPostsByStudentId(
            @PathVariable("id") Long userId,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount) {
        List<PostDTO> posts = postService.getAllPostsOfStudent(userId, pageNo, postCount);

        if (posts != null) {
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/unapproved")
    public ResponseEntity<List<PostDTO>> getUnapprovedPosts(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount) {
        List<PostDTO> posts = postService.getUnapprovedPosts(pageNo, postCount);

        if (posts != null) {
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
