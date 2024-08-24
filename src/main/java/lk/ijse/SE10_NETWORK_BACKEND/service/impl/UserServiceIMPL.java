package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import lk.ijse.SE10_NETWORK_BACKEND.util.VarList;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceIMPL implements UserService , UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public int saveUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return VarList.Not_Acceptable;
        } else {
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            userDTO.setRole("USER");
        }
        userRepository.save(modelMapper.map(userDTO, User.class));
        return VarList.Created;
    }

    @Override
    public int updateUser(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getUserId()).orElse(null);

        if (user != null) {
            if (userDTO.getPassword() != null
                    && passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {

                if (!userDTO.getNewPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(userDTO.getNewPassword()));
                }
                user.setName(userDTO.getName());
                user.setDob(userDTO.getDob());
                user.setEmail(userDTO.getEmail());
                userRepository.save(user);
                return VarList.OK;
            } else {
                return VarList.Not_Acceptable;
            }
        }
        return VarList.Not_Found;
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
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            return modelMapper.map(user, UserDTO.class);
        }
        return null;
    }

    @Override
    public List<UserDTO> getUsersWithBirthdaysToday() {
        List<User> users = userRepository.findUsersWithBirthday().orElse(null);

        if (users != null) {
            return users.stream().map(User -> modelMapper.map(User, UserDTO.class)).toList();
        }
        return null;
    }

    @Override
    public UserDTO loadUserDetailsByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return modelMapper.map(user, UserDTO.class);
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Email: " + email);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), getAuthorities(user));
    }

    /**
     * Retrieves the authorities (roles) for a given user.
     *
     * This method creates a set of granted authorities based on the user's role.
     * The authorities are used by Spring Security to enforce role-based access control.
     *
     * @param user The User object containing role information.
     * @return A set of SimpleGrantedAuthority representing the user's roles.
     */
    public Set<SimpleGrantedAuthority> getAuthorities(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        return authorities;
    }
}
