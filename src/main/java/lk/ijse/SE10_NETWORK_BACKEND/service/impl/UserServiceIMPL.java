package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import jakarta.mail.MessagingException;
import lk.ijse.SE10_NETWORK_BACKEND.customObj.MailBody;
import lk.ijse.SE10_NETWORK_BACKEND.customObj.OtpResponse;
import lk.ijse.SE10_NETWORK_BACKEND.dto.ImageUpdateDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import lk.ijse.SE10_NETWORK_BACKEND.dto.UserSearchDTO;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import lk.ijse.SE10_NETWORK_BACKEND.util.EmailUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.ImageUploadUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.VarList;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceIMPL implements UserService, UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailUtil emailUtil;

    @Override
    public int saveUser(UserDTO userDTO) {
        boolean emailExists = userRepository.existsByEmail(userDTO.getEmail());
        User user = userRepository.findById(userDTO.getUserId()).orElse(null);

        if ((user != null && !emailExists) || (user == null && emailExists)) {
            return VarList.Not_Acceptable;
        }

        if (user == null) {
            user = modelMapper.map(userDTO, User.class);
        } else {
            user.setName(userDTO.getName());
            user.setBatch(userDTO.getBatch());
            user.setDob(userDTO.getDob());
            user.setStatus("Active");
            user.setBio(userDTO.getBio());
        }

        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole("USER");
        User savedUser = userRepository.save(user);

        try {
            if (userDTO.getProfilePic() != null) {
                ImageUploadUtil.saveFile(savedUser.getUserId(), "profile", userDTO.getProfilePic());
            }
            if (userDTO.getCoverPic() != null) {
                ImageUploadUtil.saveFile(savedUser.getUserId(), "cover", userDTO.getCoverPic());
            }
            return VarList.Created;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return VarList.Internal_Server_Error;
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
                user.setBio(userDTO.getBio());
                userRepository.save(user);
                return VarList.OK;
            } else {
                return VarList.Not_Acceptable;
            }
        }
        return VarList.Not_Found;
    }

    @Override
    public boolean deleteUser(Long id) throws MessagingException, IOException {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            user.setStatus("Suspended");
            userRepository.save(user);
            Map<String, String> map = new HashMap<>();
            map.put("username", user.getName());
            emailUtil.sendHtmlMessage(
                    MailBody.builder()
                            .templateName("AccountDeactivation")
                            .to(user.getEmail())
                            .subject("Account Deactivation")
                            .replacements(map)
                            .build()
            );
            return true;
        }
        return false;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            UserDTO dto = modelMapper.map(user, UserDTO.class);
            return ImageUploadUtil.getUserImages(dto);
        }
        return null;
    }

    @Override
    public List<String> getUserNamesWithBirthdaysToday() {
        List<User> users = userRepository.findUsersWithBirthday().orElse(null);

        if (users != null) {
            List<String> list = new ArrayList<>();
            users.forEach(user -> list.add(user.getName()));
            return list;
        }
        return null;
    }

    @Override
    public List<UserSearchDTO> getUsersWithBirthdaysToday() {
        List<User> users = userRepository.findUsersWithBirthday().orElse(Collections.emptyList());

        if (!users.isEmpty()) {
            return users.stream().map(user -> {
                UserSearchDTO dto = modelMapper.map(user, UserSearchDTO.class);
                dto.setProfileImg(ImageUploadUtil.getProfileImage(user.getUserId()));
                return dto;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
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
    public boolean updateUserImage(ImageUpdateDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);

        if (user != null) {
            try {
                ImageUploadUtil.saveFile(user.getUserId(), dto.getType(), dto.getImage());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean deleteUserImage(ImageUpdateDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);

        if (user != null) {
            try {
                ImageUploadUtil.deleteFile(user.getUserId(), dto.getType());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), getAuthorities(user));
    }

    @Override
    public List<UserSearchDTO> findUsersByNameOrNameLike(String name, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<User> users = userRepository.findUsersByNameOrNameLike(name, pageable);

        if (!users.isEmpty()) {
            return users.stream().map(user -> {
                UserSearchDTO dto = modelMapper.map(user, UserSearchDTO.class);
                dto.setProfileImg(ImageUploadUtil.getProfileImage(user.getUserId()));
                return dto;
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public String getProfileImg(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userRepository.findByEmail(username).orElse(null);
        return ImageUploadUtil.getProfileImage(user.getUserId());
    }

    @Override
    public OtpResponse verifyUserEmail(String name, String email) throws MessagingException, IOException {
        System.out.println(name.equals("null"));
        String subject = "Verify Your Email";
        String templateName = "EmailVerification";
        if (name.equals("null")) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                throw new RuntimeException();
            } else {
                name = user.getName();
                subject = "Password Reset Request";
                templateName = "PasswordReset";
            }
        }
        String otp = emailUtil.otpGenerator().toString();
        Map<String, String> map = new HashMap<>();
        map.put("username", name);
        map.put("otpCode", otp);
        emailUtil.sendHtmlMessage(
                MailBody.builder()
                        .templateName(templateName)
                        .to(email)
                        .subject(subject)
                        .replacements(map)
                        .build()
        );
        return new OtpResponse(otp, email);
    }

    @Override
    public void updatePassword(String email, String password) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email " + email + " does not exist.");
        } else {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
            }
        }
    }

    /**
     * Retrieves the authorities (roles) for a given user.
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
