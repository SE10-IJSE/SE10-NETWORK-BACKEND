package lk.ijse.SE10_NETWORK_BACKEND.service.impl;

import jakarta.mail.MessagingException;
import lk.ijse.SE10_NETWORK_BACKEND.customObj.MailBody;
import lk.ijse.SE10_NETWORK_BACKEND.dto.*;
import lk.ijse.SE10_NETWORK_BACKEND.entity.User;
import lk.ijse.SE10_NETWORK_BACKEND.exception.*;
import lk.ijse.SE10_NETWORK_BACKEND.repository.UserRepository;
import lk.ijse.SE10_NETWORK_BACKEND.service.UserService;
import lk.ijse.SE10_NETWORK_BACKEND.util.EmailUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.ImageUploadUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.JwtUtil;
import lk.ijse.SE10_NETWORK_BACKEND.util.VarList;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserServiceIMPL implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailUtil emailUtil;
    private final Map<String, String> otpList = new HashMap<>();

    @Override
    public void saveUser(SignUpDTO userDTO) {
        if (otpList.containsKey(userDTO.getEmail()) && otpList.get(userDTO.getEmail()).equals(userDTO.getOtp())) {
            boolean emailExists = userRepository.existsByEmail(userDTO.getEmail());
            User user = userRepository.findById(userDTO.getUserId()).orElse(null);
            if ((user != null && !emailExists) || (user == null && emailExists)) {
                throw new UserEmailMismatchException("User and email data are inconsistent.");
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
            User savedUser;
            try {
                savedUser = userRepository.save(user);
            } catch (Exception e) {
                throw new DataPersistFailedException("Failed to save user data.");
            }
            try {
                if (userDTO.getProfilePic() != null) {
                    ImageUploadUtil.saveFile(savedUser.getUserId(), "profile", userDTO.getProfilePic());
                }
                if (userDTO.getCoverPic() != null) {
                    ImageUploadUtil.saveFile(savedUser.getUserId(), "cover", userDTO.getCoverPic());
                }
            } catch (IOException e) {
                throw new DataPersistFailedException("Failed to upload user images.");
            }
        } else throw new InvalidOtpException("Invalid OTP.");
    }
    @Override
    public void updateUser(UserDTO userDTO, String token) {
        if (userDTO.getEmail().equals(jwtUtil.getUsernameFromToken(token))) {
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
                } else {
                    throw new InvalidPasswordException("Password does not match.");
                }
            } else throw new UserNotFoundException("User not found.");
        } else throw new UserEmailMismatchException("User email mismatch.");
    }
    @Override
    public void deleteUser(Long id, String token) throws MessagingException, IOException {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            if (user.getEmail().equals(jwtUtil.getUsernameFromToken(token))) {
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
            } else throw new UserEmailMismatchException("User email mismatch.");
        } else throw new UserNotFoundException("User not found.");
    }
    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            UserDTO dto = modelMapper.map(user, UserDTO.class);
            return ImageUploadUtil.getUserImages(dto);
        } else throw new UserNotFoundException("User not found.");
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
    public void updateUserImage(ImageUpdateDTO dto, String token) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);
        if (user != null) {
            if (!user.getEmail().equals(jwtUtil.getUsernameFromToken(token))) {
                throw new UserEmailMismatchException("User email mismatch.");
            } else {
                try {
                    ImageUploadUtil.saveFile(user.getUserId(), dto.getType(), dto.getImage());
                } catch (IOException e) {
                    throw new DataPersistFailedException("Failed to update user image.");
                }
            }
        } else throw new UserNotFoundException("User not found.");
    }
    @Override
    public void deleteUserImage(ImageUpdateDTO dto, String token) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);
        if (user != null) {
            if (!user.getEmail().equals(jwtUtil.getUsernameFromToken(token))) {
                throw new UserEmailMismatchException("User email mismatch.");
            } else {
                try {
                    ImageUploadUtil.deleteFile(user.getUserId(), dto.getType());
                } catch (IOException e) {
                    throw new DataPersistFailedException("Failed to delete user image.");
                }
            }
        } else throw new UserNotFoundException("User not found.");
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
    public void verifyUserEmail(String name, String email) throws MessagingException, IOException {
        String subject = "Verify Your Email";
        String templateName = "EmailVerification";
        if (name.isEmpty()) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                throw new UserNotFoundException("User not found.");
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
        otpList.put(email, otp);
    }
    @Override
    public void updatePassword(UpdatePasswordDTO dto) {
        if (otpList.containsKey(dto.getEmail()) && otpList.get(dto.getEmail()).equals(dto.getOtp())) {
            if (!userRepository.existsByEmail(dto.getEmail())) {
                throw new UserNotFoundException("User with email " + dto.getEmail() + " does not exist.");
            } else {
                User user = userRepository.findByEmail(dto.getEmail()).orElse(null);
                if (user != null) {
                    user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                    userRepository.save(user);
                }
            }
        } else throw new InvalidOtpException("Invalid OTP.");
    }
    @Override
    public UserDTO getUserByUserEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            UserDTO dto = modelMapper.map(user, UserDTO.class);
            return ImageUploadUtil.getUserImages(dto);
        }
        return null;
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
