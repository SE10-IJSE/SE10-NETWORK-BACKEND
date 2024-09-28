package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import jakarta.transaction.Transactional;
import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Notification;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Post;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.repository.PostRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.PostService;
import lk.ijse.SE10_NETWORK_BACKEND.util.ImageUploadUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public PostDTO savePost(PostDTO postDTO, String token) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        User user = userRepository.findByEmail(username).orElse(null);

        if (user != null) {
            Post post = modelMapper.map(postDTO, Post.class);
            post.setUser(user);
            post.setStatus("PENDING");
            Post save = postRepository.save(post);
            return modelMapper.map(save, PostDTO.class);
        }
        return null;
    }

    @Override
    public PostDTO updatePost(Long postId, String content) {
        Post post = postRepository.findById(postId).orElse(null);

        if (post != null) {
            post.setContent(content);
            Post save = postRepository.save(post);
            return modelMapper.map(save, PostDTO.class);
        }
        return null;
    }

    @Override
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);

        if (post != null) {
            return modelMapper.map(post, PostDTO.class);
        }
        return null;
    }

    @Override
    @Transactional
    public PostDTO updatePostStatus(Long postId, String status, String token) {
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
                Post save = postRepository.save(post);
                return modelMapper.map(save, PostDTO.class);
            } else {
                post.setStatus(status);
                post.setVerifiedBy(user);
                Post save = postRepository.save(post);
                return modelMapper.map(save, PostDTO.class);
            }
        }
        return null;
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
        Pageable pageable= PageRequest.of(pageNo, postCount);
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        Page<Post> posts = postRepository.findUnapprovedPosts(username, pageable);

        if(!posts.isEmpty()){
            User user = userRepository.findByEmail(username).orElse(null);

            return posts.getContent().stream()
                    .map(post -> {
                        PostDTO dto = modelMapper.map(post, PostDTO.class);
                        dto.setUserName(post.getUser().getName());
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
    public boolean deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            return false;
        } else {
            postRepository.deleteById(postId);
            return true;
        }
    }
}
