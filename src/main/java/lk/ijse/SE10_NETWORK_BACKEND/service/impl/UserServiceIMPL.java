package lk.ijse.SE10_NETWORK_BACKEND.service.impl;


import lk.ijse.SE10_NETWORK_BACKEND.dto.SignInDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceIMPL implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDTO saveUser(UserDTO userDTO) {
        User save = userRepository.save(userDTO.toEntity());

        if (save != null) {
            return save.toDto();
        }
        return null;
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getUserId()).orElse(null);

        if (user != null) {
            user.setName(userDTO.getName());
            user.setPassword(userDTO.getPassword());
            user.setDob(userDTO.getDob());
            user.setEmail(userDTO.getEmail());
            User save = userRepository.save(user);

            return save.toDto();
        }
        return null;
    }

    @Override
    public boolean deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            return user.toDto();
        }
        return null;
    }

    @Override
    public List<UserDTO> getUsersWithBirthdaysToday() {
        List<User> users = userRepository.findUsersWithBirthday().orElse(null);

        if (users != null) {
            return users.stream().map(User::toDto).toList();
        }
        return null;
    }

    @Override
    public UserDTO configureUser(SignInDTO signInDTO) {
        User user =
                userRepository.findByUsernameAndPassword(signInDTO.getEmail(), signInDTO.getPassword()).orElse(null);

        if (user != null) {
            return user.toDto();
        }
        return null;
    }

}
