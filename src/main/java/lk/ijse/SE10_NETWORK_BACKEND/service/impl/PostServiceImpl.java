package lk.ijse.SE10_NETWORK_BACKEND.service.impl;


import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Post;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.repository.PostRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.PostService;
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

    @Override
    public PostDTO savePost(PostDTO postDTO) {
        User user = userRepository.findById(postDTO.getUserId()).orElse(null);

        if (user != null) {
            Post post = new Post();
            post.setUser(user);
            post.setContent(postDTO.getContent());
            Post save = postRepository.save(post);
            return save.toDto();
        }
        return null;
    }

    @Override
    public PostDTO updatePost(PostDTO postDTO) {
        Post post = postRepository.findById(postDTO.getPostId()).orElse(null);

        if (post != null) {
            post.setContent(postDTO.getContent());
            Post save = postRepository.save(post);
            return save.toDto();
        }
        return null;
    }

    @Override
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);

        if (post != null) {
            return post.toDto();
        }
        return null;
    }

    @Override
    public PostDTO updatePostStatus(Long postId, Long adminId) {
        User user = userRepository.findById(adminId).orElse(null);

        if (user != null) {
            Post post = postRepository.findById(postId).orElse(null);

            if (post != null) {
                post.setStatus(true);
                post.setApprovedBy(user);
                Post save = postRepository.save(post);
                return save.toDto();
            }
        }
        return null;
    }

    @Override
    public List<PostDTO> getAllPosts(Integer pageNo, Integer postCount) {
        Pageable pageable = PageRequest.of(pageNo, postCount);
        Page<Post> allPosts = postRepository.findAllPosts(pageable);

        if (!allPosts.isEmpty()) {
            return allPosts.getContent().stream()
                    .map(post -> post.toDto())
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<PostDTO> getAllPostsOfStudent(Long studentId, Integer pageNo, Integer postCount) {
        Pageable pageable = PageRequest.of(pageNo, postCount);
        Page<Post> postsPage = postRepository.findByStudentId(studentId, pageable);

        if (!postsPage.isEmpty()) {
            return postsPage.getContent().stream()
                    .map(Post::toDto)
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<PostDTO> getUnapprovedPosts(Integer pageNo, Integer postCount) {
        Pageable pageable= PageRequest.of(pageNo, postCount);
        Page<Post> posts = postRepository.findUnapprovedPosts(pageable);

        if(!posts.isEmpty()){
            return posts.getContent().stream()
                    .map(Post::toDto)
                    .collect(Collectors.toList());
        }
        return null;
    }
}
