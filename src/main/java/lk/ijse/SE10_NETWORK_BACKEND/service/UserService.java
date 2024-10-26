package lk.ijse.SE10_NETWORK_BACKEND.service;

import jakarta.mail.MessagingException;
import lk.ijse.SE10_NETWORK_BACKEND.dto.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface UserService {
    void saveUser(SignUpDTO userDTO);
    void updateUser(UserDTO userDTO, String token);
    void deleteUser(Long id, String token) throws MessagingException, IOException;
    UserDTO getUserByEmail(String email);
    List<String> getUserNamesWithBirthdaysToday();
    List<UserSearchDTO> getUsersWithBirthdaysToday();
    UserDTO loadUserDetailsByEmail(String email);
    void updateUserImage(ImageUpdateDTO dto, String token);
    void deleteUserImage(ImageUpdateDTO dto, String token);
    List<UserSearchDTO> findUsersByNameOrNameLike(String name, int page);
    String getProfileImg(String token);
    void verifyUserEmail(String name, String email) throws MessagingException, IOException;
    void updatePassword(UpdatePasswordDTO dto);
    UserDTO getUserByUserEmail(String email);
}