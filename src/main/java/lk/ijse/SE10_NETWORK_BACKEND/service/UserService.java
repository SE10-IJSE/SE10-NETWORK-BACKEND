package lk.ijse.SE10_NETWORK_BACKEND.service;

import lk.ijse.SE10_NETWORK_BACKEND.dto.ImageUpdateDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserSearchDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    int saveUser(UserDTO userDTO);
    int updateUser(UserDTO userDTO);
    boolean deleteUser(Long id);
    UserDTO getUserByEmail(String email);
    List<String> getUserNamesWithBirthdaysToday();
    List<UserSearchDTO> getUsersWithBirthdaysToday();
    UserDTO loadUserDetailsByEmail(String email);
    boolean updateUserImage(ImageUpdateDTO dto);
    boolean deleteUserImage(ImageUpdateDTO dto);
    List<UserSearchDTO> findUsersByNameOrNameLike(String name, int page);
}
