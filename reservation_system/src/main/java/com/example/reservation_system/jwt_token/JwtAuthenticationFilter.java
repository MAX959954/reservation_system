package com.example.reservation_system.jwt_token;

import com.example.reservation_system.model.AppUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Jwts;

import java.io.IOException;

//Create an object (bean) of that class.
//@Component = "Hey Spring, please manage this class as a bean."
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private AppUserService appUserService;
    private final String SECRET_KEY = "your-secret-key";

    @Autowired
    public void JwtAuthenticationFilter (AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        String path = request.getServletPath();
        return path.equals("/api/auth/login") || path.startsWith("/api/v1/registration");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (shouldNotFilter(request)){
            filterChain.doFilter(request , response);
            return ;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")){
            String token  = header.substring(7);
            try {
                String email = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
                UserDetails userDetails = appUserService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }catch (Exception e) {
                throw new ServletException("Invalid or expired token");
            }
        }
    }

}
