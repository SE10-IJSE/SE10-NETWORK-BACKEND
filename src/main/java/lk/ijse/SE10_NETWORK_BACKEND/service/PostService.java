package lk.ijse.SE10_NETWORK_BACKEND.service;

import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostService {
    void savePost(PostDTO postDTO, String token);
    void updatePost(Long postId, String content, String token);
    PostDTO getPostById(Long id);
    void updatePostStatus(Long postId, String status, String token);
    List<PostDTO> getAllPosts(Integer pageNo, Integer postCount, String token);
    List<PostDTO> getAllPostsOfUser(Integer pageNo, Integer postCount, String token);
    List<PostDTO> getUnapprovedPosts(Integer pageNo, Integer postCount, String token);
    void deletePost(Long postId, String token);
    List<PostDTO> getAllPostsOfUserByEmail(Integer pageNo, Integer postCount, String email,String token );
}