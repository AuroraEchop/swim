package com.gavin.shipping.config;

import com.gavin.shipping.common.ApiResponse;
import com.gavin.shipping.common.UnauthorizedException;
import com.gavin.shipping.domain.UserAccount;
import com.gavin.shipping.mapper.UserMapper;
import com.gavin.shipping.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Configuration
public class PermissionConfig implements WebMvcConfigurer {

    private final ObjectProvider<UserMapper> userMapperProvider;

    public PermissionConfig(ObjectProvider<UserMapper> userMapperProvider) {
        this.userMapperProvider = userMapperProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new WritePermissionInterceptor(userMapperProvider)).addPathPatterns("/**");
    }

    private static class WritePermissionInterceptor implements HandlerInterceptor {

        private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

        private final ObjectProvider<UserMapper> userMapperProvider;
        private final AuthService authService = new AuthService();

        private WritePermissionInterceptor(ObjectProvider<UserMapper> userMapperProvider) {
            this.userMapperProvider = userMapperProvider;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws IOException {
            if (!WRITE_METHODS.contains(request.getMethod())) {
                return true;
            }

            String path = request.getServletPath();
            if (path == null || path.isBlank()) {
                path = request.getRequestURI();
            }
            if (path.startsWith("/auth") || path.startsWith("/api/auth")) {
                return true;
            }

            UserMapper userMapper = userMapperProvider.getIfAvailable();
            if (userMapper == null) {
                return true;
            }

            try {
                UserAccount user = authService.currentUser(
                        request.getHeader("Authorization"),
                        userMapper::findByUsername
                );
                if ("ADMIN".equals(user.roleCode())) {
                    return true;
                }
                writeError(response, 403, "当前账号无权执行该操作");
                return false;
            } catch (UnauthorizedException exception) {
                writeError(response, 401, exception.getMessage());
                return false;
            }
        }

        private void writeError(HttpServletResponse response, int status, String message) throws IOException {
            response.setStatus(status);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            ApiResponse<Void> body = ApiResponse.error(status, message);
            response.getWriter().write("{\"code\":" + body.code() + ",\"message\":\"" + escapeJson(body.message()) + "\",\"data\":null}");
        }

        private String escapeJson(String value) {
            return value.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}
