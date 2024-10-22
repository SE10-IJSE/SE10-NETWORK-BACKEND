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
     * Creates a new post.
     *
     * @param dto   The PostDTO object containing the post details.
     * @param token The JWT token used to identify the user creating the post.
     * @return ResponseEntity indicating the result of the post creation.
     */
    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody PostDTO dto, @RequestHeader("Authorization") String token) {
        PostDTO postDTO = postService.savePost(dto, token);

        if (postDTO != null) {
            logger.info("Post created successfully with ID: {}", postDTO.getPostId());
            return ResponseEntity.status(HttpStatus.CREATED).body("Post created successfully");
        } else {
            logger.error("Post creation failed for user with token: {}", token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post creation failed");
        }
    }

    /**
     * Updates the content of an existing post.
     *
     * @param id      The ID of the post to be updated.
     * @param content The new content for the post.
     * @return ResponseEntity indicating the result of the update.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePostContent(@PathVariable("id") Long id, @RequestParam String content) {
        PostDTO postDTO = postService.updatePost(id, content);

        if (postDTO != null) {
            logger.info("Post with ID: {} updated successfully", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            logger.warn("No post found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Updates the status of a post (approve or decline).
     *
     * @param postId The ID of the post whose status is to be updated.
     * @param status The new status for the post (approved or declined).
     * @param token  The JWT token of the admin performing the update.
     * @return ResponseEntity indicating the result of the status update.
     */
    @PutMapping("/{postId}/status")
    public ResponseEntity<Void> updatePostStatus(
            @PathVariable("postId") Long postId,
            @RequestParam("status") String status,
            @RequestHeader("Authorization") String token) {
        PostDTO postDTO = postService.updatePostStatus(postId, status, token);

        if (postDTO != null) {
            logger.info("Post status updated to '{}' for post ID: {}", status, postId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            logger.warn("Failed to update post status for ID: {}", postId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves a post by its ID.
     *
     * @param id The ID of the post to retrieve.
     * @return ResponseEntity with the post data or a 404 status if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        PostDTO postDTO = postService.getPostById(id);

        if (postDTO != null) {
            logger.info("Post with ID: {} retrieved successfully", id);
            return ResponseEntity.status(HttpStatus.OK).body(postDTO);
        } else {
            logger.warn("No post found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves all posts with pagination support.
     *
     * @param pageNo    The page number to retrieve.
     * @param postCount The number of posts per page.
     * @param token     The JWT token to identify the user.
     * @return ResponseEntity with the list of posts or a 404 status if none are found.
     */
    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> allPosts = postService.getAllPosts(pageNo, postCount, token);

        if (allPosts != null) {
            logger.info("Retrieved {} posts for user with token: {}", allPosts.size(), token);
            return ResponseEntity.status(HttpStatus.OK).body(allPosts);
        } else {
            logger.warn("No posts found for user with token: {}", token);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves posts created by a specific user with pagination.
     *
     * @param pageNo    The page number to retrieve.
     * @param postCount The number of posts per page.
     * @param token     The JWT token identifying the user.
     * @return ResponseEntity with the list of posts or a 404 status if none are found.
     */
    @GetMapping("/user")
    public ResponseEntity<List<PostDTO>> getUserPosts(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> posts = postService.getAllPostsOfUser(pageNo, postCount, token);

        if (posts != null) {
            logger.info("Retrieved {} posts for user with token: {}", posts.size(), token);
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            logger.warn("No posts found for user with token: {}", token);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves all unapproved posts of a user with pagination.
     *
     * @param pageNo  The page number to retrieve.
     * @param postCount The number of posts per page.
     * @param email The email of the user
     * @param token The JWT token identifying the user.
     * @return ResponseEntity with the list of posts or a 404 status if none are found.
     */

    @GetMapping("/userPosts")
    public ResponseEntity<List<PostDTO>> getUserPostsByEmail(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestParam("email") String email,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> posts = postService.getAllPostsOfUserByEmail(pageNo, postCount, email,token);

        if (posts != null) {
            logger.info("Retrieved {} posts for user with email: {}", posts.size(),email);
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            logger.warn("No posts found for user with email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves all unapproved posts with pagination.
     *
     * @param pageNo    The page number to retrieve.
     * @param postCount The number of posts per page.
     * @param token     The JWT token to identify the admin user.
     * @return ResponseEntity with the list of unapproved posts or a 404 status if none are found.
     */
    @GetMapping("/unapproved")
    public ResponseEntity<List<PostDTO>> getUnapprovedPosts(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> posts = postService.getUnapprovedPosts(pageNo, postCount, token);

        if (posts != null) {
            logger.info("Retrieved {} unapproved posts", posts.size());
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            logger.warn("No unapproved posts found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Deletes a post by its ID.
     *
     * @param id The ID of the post to delete.
     * @return ResponseEntity indicating the result of the deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        boolean deleted = postService.deletePost(id);

        if (deleted) {
            logger.info("Post with ID: {} deleted successfully", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            logger.error("Failed to delete post with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
