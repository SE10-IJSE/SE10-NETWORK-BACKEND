package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import jakarta.transaction.Transactional;
import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Notification;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Post;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.exception.DataPersistFailedException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.PostNotFoundException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserNotAcceptableException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.UserNotFoundException;
import lk.ijse.SE10_NETWORK_BACKEND.repository.NotificationRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.PostRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.PostService;
import lk.ijse.SE10_NETWORK_BACKEND.util.ImageUploadUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;
    @Override
    public void savePost(PostDTO postDTO, String token) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        User user = userRepository.findByEmail(username).orElse(null);
        if (user != null) {
            Post post = modelMapper.map(postDTO, Post.class);
            post.setUser(user);
            post.setStatus("PENDING");
            try {
                postRepository.save(post);
            } catch (Exception e) {
                throw new DataPersistFailedException("Failed to save post");
            }
        } else throw new UserNotFoundException("User not found");
    }
    @Override
    public void updatePost(Long postId, String content, String token) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post != null && post.getUser().getEmail().equals(jwtUtil.getUsernameFromToken(token))) {
            try {
                post.setContent(content);
                post.setStatus("PENDING");
                post.setVerifiedBy(null);
                post.setNotification(null);
                postRepository.save(post);
            } catch (Exception e) {
                throw new DataPersistFailedException("Failed to update post");
            }
        } else throw new PostNotFoundException("Post not found");
    }
    @Override
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            PostDTO map = modelMapper.map(post, PostDTO.class);
            map.setVerifiedBy(post.getVerifiedBy() != null ? post.getVerifiedBy().getName() : null);
            return map;
        }
        return null;
    }
    @Override
    @Transactional
    public void updatePostStatus(Long postId, String status, String token) {
        if (jwtUtil.getRoleFromToken(token.substring(7)).equals("ADMIN")) {
            String username = jwtUtil.getUsernameFromToken(token.substring(7));
            User user = userRepository.findByEmail(username).orElse(null);
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null) {
                if (status.equals("APPROVED")) {
                    post.setStatus(status);
                    post.setVerifiedBy(user);
                    Notification notification = new Notification(
                            "Your post has been approved",
                            "APPROVED", post.getUser()
                    );
                    post.setNotification(notification);
                    notification.setPost(post);
                } else {
                    post.setStatus(status);
                    post.setVerifiedBy(user);
                }
                try {
                    postRepository.save(post);
                } catch (Exception e) {
                    throw new DataPersistFailedException("Failed to update post status");
                }
            } else throw new PostNotFoundException("Post not found");
        } else throw new UserNotAcceptableException("User not an admin");
    }
    @Override
    public List<PostDTO> getAllPosts(Integer pageNo, Integer postCount, String token) {
        Pageable pageable = PageRequest.of(pageNo, postCount);
        Page<Post> allPosts = postRepository.findAllPosts(pageable);
        if (!allPosts.isEmpty()) {
            String username = jwtUtil.getUsernameFromToken(token.substring(7));
            User user = userRepository.findByEmail(username).orElse(null);
            return allPosts.getContent().stream()
                    .map(post -> {
                        PostDTO dto = modelMapper.map(post, PostDTO.class);
                        dto.setUserName(post.getUser().getName());
                        dto.setVerifiedBy(post.getVerifiedBy() != null ? post.getVerifiedBy().getName() : null);
                        dto.setProfileImg(ImageUploadUtil.getProfileImage(post.getUser().getUserId()));
                        dto.setInspirationCount(post.getInspires().size());
                        boolean isInspired = post.getInspires().stream()
                                .anyMatch(inspire -> inspire.getUser().getUserId().equals(user.getUserId()));
                        dto.setInspired(isInspired);
                        return dto;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }
    @Override
    public List<PostDTO> getAllPostsOfUser(Integer pageNo, Integer postCount, String token) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        User user = userRepository.findByEmail(username).orElse(null);
        Pageable pageable = PageRequest.of(pageNo, postCount);
        Page<Post> postsPage = postRepository.findByStudentId(user.getUserId(), pageable);
        if (!postsPage.isEmpty()) {
            return postsPage.getContent().stream()
                    .map(post -> {
                        PostDTO dto = modelMapper.map(post, PostDTO.class);
                        dto.setUserName(post.getUser().getName());
                        dto.setProfileImg(ImageUploadUtil.getProfileImage(post.getUser().getUserId()));
                        dto.setInspirationCount(post.getInspires().size());
                        dto.setVerifiedBy(post.getVerifiedBy() != null ? post.getVerifiedBy().getName() : null);
                        boolean isInspired = post.getInspires().stream()
                                .anyMatch(inspire -> inspire.getUser().getUserId().equals(user.getUserId()));
                        dto.setInspired(isInspired);
                        return dto;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }
    @Override
    public List<PostDTO> getUnapprovedPosts(Integer pageNo, Integer postCount, String token) {
        if (jwtUtil.getRoleFromToken(token.substring(7)).equals("ADMIN")) {
            Pageable pageable= PageRequest.of(pageNo, postCount);
            String username = jwtUtil.getUsernameFromToken(token.substring(7));
            Page<Post> posts = postRepository.findUnapprovedPosts(username, pageable);
            if(!posts.isEmpty()){
                User user = userRepository.findByEmail(username).orElse(null);
                return posts.getContent().stream()
                        .map(post -> {
                            PostDTO dto = modelMapper.map(post, PostDTO.class);
                            dto.setUserName(post.getUser().getName());
                            dto.setVerifiedBy(post.getVerifiedBy() != null ? post.getVerifiedBy().getName() : null);
                            dto.setProfileImg(ImageUploadUtil.getProfileImage(post.getUser().getUserId()));
                            dto.setInspirationCount(post.getInspires().size());
                            boolean isInspired = post.getInspires().stream()
                                    .anyMatch(inspire -> inspire.getUser().getUserId().equals(user.getUserId()));
                            dto.setInspired(isInspired);
                            return dto;
                        })
                        .collect(Collectors.toList());
            }
        }
        return null;
    }
    @Override
    public List<PostDTO> getAllPostsOfUserByEmail(Integer pageNo, Integer postCount, String email,String token) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        User user = userRepository.findByEmail(username).orElse(null);
        Pageable pageable  = PageRequest.of(pageNo, postCount);
        Page<Post>  postsPage  = postRepository.findByEmail(email, pageable);
        if (!postsPage.isEmpty()) {
            return postsPage.getContent().stream()
                    .map(post -> {
                        PostDTO dto = modelMapper.map(post, PostDTO.class);
                        dto.setUserName(post.getUser().getName());
                        dto.setProfileImg(ImageUploadUtil.getProfileImage(post.getUser().getUserId()));
                        dto.setInspirationCount(post.getInspires().size());
                        dto.setVerifiedBy(post.getVerifiedBy() != null ? post.getVerifiedBy().getName() : null);
                        boolean isInspired = post.getInspires().stream()
                                .anyMatch(inspire -> inspire.getUser().getEmail().equals(user.getEmail()));
                        dto.setInspired(isInspired);
                        return dto;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }
    @Override
    public void deletePost(Long postId, String token) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null && !post.getUser().getEmail().equals(jwtUtil.getUsernameFromToken(token))) {
            throw new PostNotFoundException("Post not found");
        } else {
            postRepository.deleteById(postId);
        }
    }
}