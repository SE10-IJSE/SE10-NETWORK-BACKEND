package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import jakarta.transaction.Transactional;
import lk.ijse.SE10_NETWORK_BACKEND.dto.InspireDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Inspire;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Post;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.exception.DataPersistFailedException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.InspireNotFoundException;
import lk.ijse.SE10_NETWORK_BACKEND.exception.InvalidInspirationException;
import lk.ijse.SE10_NETWORK_BACKEND.repository.InspireRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.PostRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.InspireService;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class InspireServiceIMPL implements InspireService {
    private final InspireRepository inspireRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    @Override
    public void saveInspiration(InspireDTO inspireDTO, String token) {
        User user =
                userRepository.findByEmail(jwtUtil.getUsernameFromToken(token.substring(7))).orElse(null);
        Post post = postRepository.findById(inspireDTO.getPostId()).orElse(null);
        if (user == null || post == null || post.getStatus().equals("PENDING") || post.getStatus().equals("DECLINED"))
            throw new InvalidInspirationException("User or Post not found");
        Inspire inspire = modelMapper.map(inspireDTO, Inspire.class);
        inspire.setUser(user);
        inspire.setPost(post);
        try {
            inspireRepository.save(inspire);
        } catch (Exception e) {
            throw new DataPersistFailedException("Failed to save inspiration");
        }
    }
    @Override
    public void deleteInspiration(Long postId, String token) {
        inspireRepository.getInspiresByPostIdAndEmail(
                postId,
                jwtUtil.getUsernameFromToken(token.substring(7))
        ).ifPresentOrElse(inspire -> {
                    Post post = inspire.getPost();
                    post.getInspires().remove(inspire);
                    postRepository.save(post);
                    inspireRepository.delete(inspire);
                }, () -> { throw new InspireNotFoundException("Inspiration not found"); }
        );
    }
}