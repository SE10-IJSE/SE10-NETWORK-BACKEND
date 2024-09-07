package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import lk.ijse.SE10_NETWORK_BACKEND.dto.InspireDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Inspire;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Post;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.repository.InspireRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.PostRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.InspireService;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InspireServiceIMPL implements InspireService {
    @Autowired
    private InspireRepository inspireRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public InspireDTO saveInspiration(InspireDTO inspireDTO, String token) {
        User user =
                userRepository.findByEmail(jwtUtil.getUsernameFromToken(token.substring(7))).orElse(null);
        Post post = postRepository.findById(inspireDTO.getPostId()).orElse(null);

        if (user != null || post != null) {
            Inspire inspire = modelMapper.map(inspireDTO, Inspire.class);
            inspire.setUser(user);
            inspire.setPost(post);

            return modelMapper.map(inspireRepository.save(inspire), InspireDTO.class);
        }
        return null;
    }

    @Override
    public boolean deleteInspiration(Long postId, String token) {
        Inspire inspire = inspireRepository.getInspiresByPostIdAndEmail(
                postId,
                jwtUtil.getUsernameFromToken(token.substring(7))
        ).orElse(null);

        if (inspire != null) {
            inspireRepository.delete(inspire);
            return true;
        }
        return false;
    }
}
