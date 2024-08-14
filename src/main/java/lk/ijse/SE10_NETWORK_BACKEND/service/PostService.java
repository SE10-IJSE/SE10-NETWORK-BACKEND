package lk.ijse.SE10_NETWORK_BACKEND.service;


import lk.ijse.SE10_NETWORK_BACKEND.dto.PostDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostService {
    PostDTO savePost(PostDTO postDTO);
    PostDTO updatePost(PostDTO postDTO);
    PostDTO getPostById(Long id);
    PostDTO updatePostStatus(Long postId, Long adminId);
    List<PostDTO> getAllPosts(Integer pageNo, Integer postCount);
    List<PostDTO> getAllPostsOfStudent(Long studentId, Integer pageNo, Integer postCount);
    List<PostDTO> getUnapprovedPosts(Integer pageNo, Integer postCount);
}
