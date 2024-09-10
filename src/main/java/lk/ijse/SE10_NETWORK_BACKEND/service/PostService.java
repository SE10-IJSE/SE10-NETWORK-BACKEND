package lk.ijse.SE10_NETWORK_BACKEND.service;

import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostService {
    PostDTO savePost(PostDTO postDTO, String token);
    PostDTO updatePost(Long postId, String content);
    PostDTO getPostById(Long id);
    PostDTO updatePostStatus(Long postId, String status, String token);
    List<PostDTO> getAllPosts(Integer pageNo, Integer postCount, String token);
    List<PostDTO> getAllPostsOfUser(Integer pageNo, Integer postCount, String token);
    List<PostDTO> getUnapprovedPosts(Integer pageNo, Integer postCount, String token);
    boolean deletePost(Long postId);
}
