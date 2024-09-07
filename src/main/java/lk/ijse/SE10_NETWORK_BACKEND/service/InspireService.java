package lk.ijse.SE10_NETWORK_BACKEND.service;

import lk.ijse.SE10_NETWORK_BACKEND.dto.InspireDTO;
import org.springframework.stereotype.Service;

@Service
public interface InspireService {
    InspireDTO saveInspiration(InspireDTO inspireDTO, String token);
    boolean deleteInspiration(Long postId, String token);
}
