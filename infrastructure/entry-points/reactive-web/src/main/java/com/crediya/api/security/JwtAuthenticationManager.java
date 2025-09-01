package com.crediya.api.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;

public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationManager(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        if (!jwtUtil.validateToken(authToken)) {
            return Mono.empty();
        }

        String username = jwtUtil.getUsername(authToken);
        String role = jwtUtil.getRole(authToken);

        AbstractAuthenticationToken auth = getAbstractAuthenticationToken(role, authToken, username);

        auth.setAuthenticated(true);
        return Mono.just(auth);
    }

    private static AbstractAuthenticationToken getAbstractAuthenticationToken(String role, String authToken, String username) {
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return
                new AbstractAuthenticationToken(authorities) {
            @Override
            public Object getCredentials() {
                return authToken;
            }

            @Override
            public Object getPrincipal() {
                return username;
            }
        };
    }

}
