package re.edu.identityservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import re.edu.identityservice.dto.request.UserLoginRequest;
import re.edu.identityservice.dto.request.UserRegisterRequest;
import re.edu.identityservice.dto.response.TokenResponse;
import re.edu.identityservice.dto.response.UserResponse;
import re.edu.identityservice.entity.User;
import re.edu.identityservice.repository.UserRepository;
import re.edu.identityservice.util.AuthConstant;
import re.edu.identityservice.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tài khoản đã tồn tại trên hệ thống!");
        }

        // Thực hiện băm bảo mật mật khẩu thô bằng BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        String userRole = (request.getRole() != null && !request.getRole().isEmpty())
                ? request.getRole() : "USER";

        User user = User.builder()
                .username(request.getUsername())
                .password(hashedPassword)
                .role(userRole)
                .build();

        User savedUser = userRepository.save(user);

        // Chuyển đổi sang Response mẫu không lộ mật khẩu ra ngoài
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .build();
    }


    public String generateTestToken(String username) {
        // Tìm user trong database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với username: " + username));

        // Gọi util để tạo JWT
        return jwtUtil.generateToken(user);
    }

    public TokenResponse login(UserLoginRequest request) {
        // 1. Tìm kiếm User trong Database
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException(AuthConstant.BAD_CREDENTIALS_MESSAGE));

        // 2. So khớp mật khẩu thô với mật khẩu đã hash trong Database
        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isPasswordMatch) {
            throw new BadCredentialsException(AuthConstant.BAD_CREDENTIALS_MESSAGE);
        }

        // 3. Nếu hợp lệ, cấp phát JWT
        String token = jwtUtil.generateToken(user);
        return new TokenResponse(token);
    }
}