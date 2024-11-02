package lk.ijse.SE10_NETWORK_BACKEND.controller;

import jakarta.validation.Valid;
import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import lk.ijse.SE10_NETWORK_BACKEND.exception.DataPersistFailedException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.PostNotFoundException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserNotAcceptableException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserNotFoundException;
import lk.ijse.SE10_NETWORK_BACKEND.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    private final PostService postService;
    /**
     * Creates a new post.
     *
     * @param dto   The PostDTO object containing the post details.
     * @param token The JWT token used to identify the user creating the post.
     * @return A ResponseEntity indicating the result of the post creation with status 201 (Created) on success, 404 (Not Found) if the user is not found, 400 (Bad Request) on data persistence failure, and 500 (Internal Server Error) for other errors.
     */
    @PostMapping
    public ResponseEntity<String> createPost(
            @Valid @RequestBody PostDTO dto,
            @RequestHeader("Authorization") String token) {
        try {
            postService.savePost(dto, token);
            log.info("Post created successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).body("Post created successfully");
        } catch (UserNotFoundException e) {
            log.error("User not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (DataPersistFailedException e) {
            log.error("Post creation failed due to data persistence issue.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post creation failed");
        } catch (Exception e) {
            log.error("Something went wrong while creating post.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Post creation failed");
        }
    }
    /**
     * Updates the content of an existing post.
     *
     * @param id      The ID of the post to be updated.
     * @param content The new content for the post.
     * @param token   The JWT token used to identify the user updating the post.
     * @return A ResponseEntity indicating the result of the update with status 204 (No Content) on success, 404 (Not Found) if the post is not found, and 400 (Bad Request) on data persistence failure.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePostContent(
            @PathVariable("id") Long id,
            @RequestBody String content,
            @RequestHeader("Authorization") String token) {
        try {
            postService.updatePost(id, content, token.substring(7));
            log.info("Post with ID: {} updated successfully", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (PostNotFoundException e) {
            log.warn("No post found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataPersistFailedException e) {
            log.error("Failed to update post with ID: {}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Something went wrong while updating post with ID: {}", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /**
     * Updates the status of a post (approve or decline).
     *
     * @param postId The ID of the post whose status is to be updated.
     * @param status The new status for the post (approved or declined).
     * @param token  The JWT token of the admin performing the update.
     * @return A ResponseEntity indicating the result of the status update with status 204 (No Content) on success, 404 (Not Found) if the post is not found, 400 (Bad Request) on data persistence failure, and 406 (Not Acceptable) if the user is not an admin.
     */
    @PutMapping("/{postId}/status")
    public ResponseEntity<Void> updatePostStatus(
            @PathVariable("postId") Long postId,
            @RequestParam("status") String status,
            @RequestHeader("Authorization") String token) {
        try {
            postService.updatePostStatus(postId, status, token);
            log.info("Status of post with ID: {} updated successfully", postId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (PostNotFoundException e) {
            log.warn("No post found with ID: {}", postId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataPersistFailedException e) {
            log.error("Failed to update status of post with ID: {}", postId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (UserNotAcceptableException e) {
            log.error("User not an admin to update post status with ID: {}", postId);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }
    /**
     * Deletes a post by its ID.
     *
     * @param id The ID of the post to delete.
     * @param token The JWT token used to identify the user deleting the post.
     * @return A ResponseEntity indicating the result of the deletion with status 204 (No Content) on success, 404 (Not Found) if the post is not found, and 500 (Internal Server Error) for other errors.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // TODO: validate if the user is the owner of the post
            postService.deletePost(id, token.substring(7));
            log.info("Post with ID: {} deleted successfully", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (PostNotFoundException e) {
            log.warn("No post found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Something went wrong while deleting post with ID: {}", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /**
     * Retrieves a post by its ID.
     *
     * @param id The ID of the post to retrieve.
     * @return A ResponseEntity with status 200 (OK) and the post data on success, and 404 (Not Found) if the post is not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        PostDTO postDTO = postService.getPostById(id);
        if (postDTO != null) {
            log.info("Post with ID: {} retrieved successfully", id);
            return ResponseEntity.status(HttpStatus.OK).body(postDTO);
        } else {
            log.warn("No post found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    /**
     * Retrieves all posts with pagination support.
     *
     * @param pageNo    The page number to retrieve.
     * @param postCount The number of posts per page.
     * @param token     The JWT token to identify the user.
     * @return A ResponseEntity with status 200 (OK) and a list of posts on success, and 404 (Not Found) if no posts are found.
     */
    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> allPosts = postService.getAllPosts(pageNo, postCount, token);
        if (allPosts != null) {
            log.info("Retrieved {} posts for user.", allPosts.size());
            return ResponseEntity.status(HttpStatus.OK).body(allPosts);
        } else {
            log.warn("No posts found for user.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    /**
     * Retrieves posts created by a specific user with pagination.
     *
     * @param pageNo    The page number to retrieve.
     * @param postCount The number of posts per page.
     * @param token     The JWT token identifying the user.
     * @return A ResponseEntity with status 200 (OK) and a list of posts on success, and 404 (Not Found) if no posts are found.
     */
    @GetMapping("/user")
    public ResponseEntity<List<PostDTO>> getUserPosts(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> posts = postService.getAllPostsOfUser(pageNo, postCount, token);
        if (posts != null) {
            log.info("Retrieved {} posts for user.", posts.size());
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            log.warn("No posts found for user.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    /**
     * Retrieves all unapproved posts with pagination.
     *
     * @param pageNo    The page number to retrieve.
     * @param postCount The number of posts per page.
     * @param token     The JWT token to identify the admin user.
     * @return A ResponseEntity with status 200 (OK) and a list of unapproved posts on success, and 404 (Not Found) if no unapproved posts are found.
     */
    @GetMapping("/unapproved")
    public ResponseEntity<List<PostDTO>> getUnapprovedPosts(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> posts = postService.getUnapprovedPosts(pageNo, postCount, token);
        if (posts != null) {
            log.info("Retrieved {} unapproved posts", posts.size());
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            log.warn("No unapproved posts found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    /**
     * Retrieves all unapproved posts of a user with pagination.
     *
     * @param pageNo    The page number to retrieve.
     * @param postCount The number of posts per page.
     * @param email     The email of the user.
     * @param token     The JWT token identifying the user.
     * @return A ResponseEntity with status 200 (OK) and a list of posts on success, and 404 (Not Found) if no posts are found.
     */
    @GetMapping("/userPosts")
    public ResponseEntity<List<PostDTO>> getUserPostsByEmail(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("postCount") Integer postCount,
            @RequestParam("email") String email,
            @RequestHeader("Authorization") String token) {
        List<PostDTO> posts = postService.getAllPostsOfUserByEmail(pageNo, postCount, email, token);

        if (posts != null) {
            log.info("Retrieved {} posts for user with email: {}", posts.size(), email);
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } else {
            log.warn("No posts found for user with email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}