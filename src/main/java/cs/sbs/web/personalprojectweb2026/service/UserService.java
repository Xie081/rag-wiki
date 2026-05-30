package cs.sbs.web.personalprojectweb2026.service;

import cs.sbs.web.personalprojectweb2026.model.dto.AuthResponse;
import cs.sbs.web.personalprojectweb2026.model.dto.LoginRequest;
import cs.sbs.web.personalprojectweb2026.model.dto.RegisterRequest;
import cs.sbs.web.personalprojectweb2026.model.entity.User;
import cs.sbs.web.personalprojectweb2026.repository.UserRepository;
import cs.sbs.web.personalprojectweb2026.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return new AuthResponse(token, new AuthResponse.UserInfo(user.getId(), user.getUsername(), user.getEmail()));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return new AuthResponse(token, new AuthResponse.UserInfo(user.getId(), user.getUsername(), user.getEmail()));
    }
}
