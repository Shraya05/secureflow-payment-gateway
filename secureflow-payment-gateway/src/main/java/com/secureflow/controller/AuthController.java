package com.secureflow.controller;

import ch.qos.logback.classic.Logger;
import com.secureflow.dto.AuthRequest;
import com.secureflow.dto.AuthResponse;
import com.secureflow.service.UserService;
import com.secureflow.security.JwtUtil;
import com.secureflow.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest authRequest) {
        userService.registerUser(new User(
                authRequest.getUsername(),
                authRequest.getPassword(),
                authRequest.getEmail(), // email would be in a real implementation
                authRequest.getRole()  // role would be set in service
        ));
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        System.out.println(token);

        return ResponseEntity.ok(new AuthResponse(
                token,
                userDetails.getUsername(),
                ((User) userDetails).getRole()
        ));
    }

    @GetMapping("/validate-test")
    public ResponseEntity<?> validateTest(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtUtil.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return ResponseEntity.ok(claims);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid token: " + e.getMessage());
        }
    }
}