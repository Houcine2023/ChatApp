package com.SummerProject.Skote.Configurations;

import com.SummerProject.Skote.Services.UserServiceImpl;
import com.SummerProject.Skote.models.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    private UserServiceImpl userService;

    @Autowired
    public void setUserService(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (userService == null) {
            logger.error("UserServiceImpl is not injected");
            return false;
        }

        String userIdStr = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("userId");

        logger.info("Received userId from query param: {}", userIdStr);

        if (userIdStr != null) {
            try {
                UUID userId = UUID.fromString(userIdStr);
                boolean userExists = userService.existsById(userId);
                if (userExists) {
                    UserPrincipal userPrincipal = new UserPrincipal(userId);

                    // Store principal in session attributes
                    attributes.put("principal", userPrincipal);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userPrincipal, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return true;
                }
                logger.warn("User not found for userId: {}", userIdStr);
                return false;
            } catch (IllegalArgumentException e) {
                logger.error("Invalid userId format: {}", userIdStr, e);
                return false;
            }
        }

        logger.warn("No userId found in query parameters");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("Handshake failed", exception);
        }
    }
}
