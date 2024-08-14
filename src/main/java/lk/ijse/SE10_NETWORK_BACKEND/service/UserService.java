package lk.ijse.SE10_NETWORK_BACKEND.service;

import lk.ijse.SE10_NETWORK_BACKEND.dto.SignInDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    UserDTO saveUser(UserDTO userDTO);
    UserDTO updateUser(UserDTO userDTO);
    boolean deleteUser(Long id);
    UserDTO getUserById(Long id);
    List<UserDTO> getUsersWithBirthdaysToday();
    UserDTO configureUser(SignInDTO signInDTO);
}
