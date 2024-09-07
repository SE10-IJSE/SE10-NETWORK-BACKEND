package lk.ijse.SE10_NETWORK_BACKEND.controller;

import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import lk.ijse.SE10_NETWORK_BACKEND.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
@CrossOrigin("*")
public class PostController {

    @Autowired
    private PostService postService;

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    /**
     * Saves a new post.
     *
     * @param dto   The PostDTO object containing the post data.
     * @param token The JWT token for find user.
     * @return ResponseEntity with HTTP status and message.
     */
    @PostMapping
    public ResponseEntity<String> savePost(@RequestBody PostDTO dto, @RequestHeader("Authorization") String token) {
        PostDTO postDTO = postService.savePost(dto, token);

        if (postDTO != null) {
            logger.info("Post saved successfully with id: {}", dto.getPostId());
            return ResponseEntity.status(HttpStatus.CREATED).body("Post saved successfully");
        } else {
            logger.warn("Failed to save post");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post could not be saved");
        }
    }

    /**
     * Updates an existing post.
     *
     * @param dto The PostDTO object with updated data.
     * @return ResponseEntity with HTTP status and message.
     */
    @PutMapping
    public ResponseEntity<String> updatePost(@RequestBody PostDTO dto) {
        PostDTO postDTO = postService.updatePost(dto);

        if (postDTO != null) {
            logger.info("Post updated successfully with ID: {}", dto.getPostId());
            return ResponseEntity.status(HttpStatus.OK).body("Post updated successfully");
        } else {
            logger.warn("Failed to update post with ID: {}", dto.getPostId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post could not be updated");
        }
    }

    /**
     * Updates the status of a post.
     *
     * @param postId  The ID of the post.
     * @param adminId The ID of the admin approving the post.
     * @return ResponseEntity with HTTP status and message.
     */
    @PutMapping("/{postId}")
    public ResponseEntity<String> updateStatus(
            @PathVariable("postId") Long postId,
            @RequestParam("adminId") Long adminId) {
        PostDTO postDTO = postService.updatePostStatus(postId, adminId);

        if (postDTO != null) {
            logger.info("Post status updated successfully for post ID: {}", postId);
            return ResponseEntity.status(HttpStatus.OK).body("Post status updated successfully");
        } else {
            logger.warn("Failed to update status for post ID: {}", postId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post could not be updated");
        }
    }

    /**
     * Retrieves a post by its ID.
     *
     * @param id The ID of the post to retrieve.
     * @return ResponseEntity with the PostDTO object or a 404 status.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id) {
        PostDTO postById = postService.getPostById(id);

        if (postById != null) {
            logger.info("Post fetched successfully with ID: {}", id);
            return ResponseEntity.status(HttpStatus.OK).body(postById);
        } else {
            logger.warn("Post not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Retrieves all posts with pagination.
     *
     * @param pageNo    The page number.
     * @param postCount The number of posts per page.
     * @param token     The JWT token for find user.
     * @return ResponseEntity with a list of PostDTO objects or a 404 status.
     */
    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPost(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> allPosts = postService.getAllPosts(pageNo, postCount, token);

        if (allPosts != null) {
            logger.info("Fetched {} posts", allPosts.size());
            return ResponseEntity.status(HttpStatus.OK).body(allPosts);
        } else {
            logger.warn("No posts found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Retrieves posts for a specific user with pagination.
     *
     * @param pageNo    The page number.
     * @param postCount The number of posts per page.
     * @param token     The JWT token for find user.
     * @return ResponseEntity with a list of PostDTO objects or a 404 status.
     */
    @GetMapping("/user")
    public ResponseEntity<List<PostDTO>> getPostsByStudentId(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> posts = postService.getAllPostsOfUser(pageNo, postCount, token);

        if (posts != null) {
            logger.info("Fetched {} posts for user", posts.size());
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            logger.warn("No posts found for user");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Retrieves all unapproved posts with pagination.
     *
     * @param pageNo    The page number.
     * @param postCount The number of posts per page.
     * @return ResponseEntity with a list of PostDTO objects or a 404 status.
     */
    @GetMapping("/unapproved")
    public ResponseEntity<List<PostDTO>> getUnapprovedPosts(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount) {
        List<PostDTO> posts = postService.getUnapprovedPosts(pageNo, postCount);

        if (posts != null) {
            logger.info("Fetched {} unapproved posts", posts.size());
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            logger.warn("No unapproved posts found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
