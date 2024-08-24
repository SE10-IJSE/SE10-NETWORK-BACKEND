package lk.ijse.SE10_NETWORK_BACKEND.service;

import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    int saveUser(UserDTO userDTO);
    int updateUser(UserDTO userDTO);
    boolean deleteUser(Long id);
    UserDTO getUserByEmail(String email);
    List<UserDTO> getUsersWithBirthdaysToday();
    UserDTO loadUserDetailsByEmail(String email);
}
