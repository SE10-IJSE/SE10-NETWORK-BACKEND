package lk.ijse.SE10_NETWORK_BACKEND.service.impl;


import lk.ijse.SE10_NETWORK_BACKEND.dto.SignInDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import lk.ijse.SE10_NETWORK_BACKEND.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceIMPL implements UserService , UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public int saveUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return VarList.Not_Acceptable;
        } else {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            userDTO.setRole("USER");
        }
        userRepository.save(userDTO.toEntity());
        return VarList.Created;
        //User save = userRepository.save(userDTO.toEntity());

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


    @Override
    public UserDTO loadUserDetailsByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return user.toDto();
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), getAuthorities(user));
    }

    public Set<SimpleGrantedAuthority> getAuthorities(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        return authorities;
    }
}
