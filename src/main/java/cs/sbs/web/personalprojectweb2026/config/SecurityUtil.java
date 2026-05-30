package cs.sbs.web.personalprojectweb2026.config;

import cs.sbs.web.personalprojectweb2026.model.entity.User;
import cs.sbs.web.personalprojectweb2026.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户未登录"));
    }
}
