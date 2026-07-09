package re.edu.identityservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import re.edu.identityservice.dto.request.UserLoginRequest;
import re.edu.identityservice.dto.request.UserRegisterRequest;
import re.edu.identityservice.dto.response.TokenResponse;
import re.edu.identityservice.dto.response.UserResponse;
import re.edu.identityservice.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRegisterRequest request) {
        UserResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/test-token")
    public ResponseEntity<Map<String, String>> getTestToken(@RequestParam("username") String username) {
        String token = userService.generateTestToken(username);
        // Trả về dưới dạng JSON cho dễ nhìn
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            TokenResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            // Bắt lỗi BadCredentialsException ném ra từ Service và trả về mã 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}