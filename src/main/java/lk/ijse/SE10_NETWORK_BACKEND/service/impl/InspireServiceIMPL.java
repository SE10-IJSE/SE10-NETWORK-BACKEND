package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import lk.ijse.SE10_NETWORK_BACKEND.dto.InspireDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Inspire;
import lk.ijse.SE10_NETWORK_BACKEND.entity.Post;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.repository.InspireRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.PostRepository;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.InspireService;
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

    @Override
    public InspireDTO saveInspiration(InspireDTO inspireDTO) {
        User user = userRepository.findById(inspireDTO.getUserId()).orElse(null);
        Post post = postRepository.findById(inspireDTO.getPostId()).orElse(null);

        if (user != null || post != null) {
            Inspire inspire = new Inspire();
            inspire.setUser(user);
            inspire.setPost(post);
            Inspire save = inspireRepository.save(inspire);

            return save.toDTO();
        }
        return null;
    }

    @Override
    public boolean deleteInspiration(Long inspireId) {
        Inspire inspire = inspireRepository.findById(inspireId).orElse(null);

        if (inspire != null) {
            inspireRepository.delete(inspire);
            return true;
        }
        return false;
    }
}
