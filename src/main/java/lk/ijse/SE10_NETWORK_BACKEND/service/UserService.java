package lk.ijse.SE10_NETWORK_BACKEND.service;

import jakarta.mail.MessagingException;
import lk.ijse.SE10_NETWORK_BACKEND.customObj.OtpResponse;
import lk.ijse.SE10_NETWORK_BACKEND.dto.ImageUpdateDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserSearchDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface UserService {
    int saveUser(UserDTO userDTO);
    int updateUser(UserDTO userDTO);
    boolean deleteUser(Long id) throws MessagingException, IOException;
    UserDTO getUserByEmail(String email);
    List<String> getUserNamesWithBirthdaysToday();
    List<UserSearchDTO> getUsersWithBirthdaysToday();
    UserDTO loadUserDetailsByEmail(String email);
    boolean updateUserImage(ImageUpdateDTO dto);
    boolean deleteUserImage(ImageUpdateDTO dto);
    List<UserSearchDTO> findUsersByNameOrNameLike(String name, int page);
    String getProfileImg(String token);
    OtpResponse verifyUserEmail(String name, String email) throws MessagingException, IOException;
    void updatePassword(String email, String password);
}
