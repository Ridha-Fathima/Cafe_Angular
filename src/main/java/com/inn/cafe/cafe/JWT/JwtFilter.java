package com.inn.cafe.cafe.JWT;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final CustomerUserDetailsService customerUsersDetailsService;
    
    private static Claims claims = null;
        private static String username = null;
            
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
            
                    // Skip authentication for these endpoints
                    if (request.getServletPath().matches("/user/login|/user/signup|/user/forgotPassword")) {
                        filterChain.doFilter(request, response);
                        return;
                    }
            
                    String authorizationHeader = request.getHeader("Authorization");
            
                    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        filterChain.doFilter(request, response);
                        return;
                    }
            
                    String token = authorizationHeader.substring(7);
                    username = jwtUtil.extractUserName(token);
                    claims = jwtUtil.extractAllClaims(token);
            
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = customerUsersDetailsService.loadUserByUsername(username);
            
                        if (jwtUtil.validateToken(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
            
                    filterChain.doFilter(request, response);
                }
            
                public static boolean isAdmin() {
                    return claims != null && "admin".equalsIgnoreCase(String.valueOf(claims.get("role")));
            }
        
            public boolean isUser() {
                return claims != null && "user".equalsIgnoreCase(String.valueOf(claims.get("role")));
            }
        
            public static String getCurrentUser() {
                return username;
    }
}
