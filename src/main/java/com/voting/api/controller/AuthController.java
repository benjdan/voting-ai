/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.controller;

import com.voting.api.dto.ApiResponse;
import com.voting.api.dto.AuthResponse;
import com.voting.api.dto.LoginRequest;
import com.voting.api.dto.RegisterRequest;
import com.voting.application.usecase.RegisterUserUseCase;
import com.voting.domain.model.User;
import com.voting.domain.port.UserRepository;
import com.voting.infrastructure.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@CrossOrigin(origins = "*")
// @Slf4j
public class AuthController {
    
	@Autowired
    private RegisterUserUseCase registerUserUseCase;
	@Autowired
    private AuthenticationManager authenticationManager;
	@Autowired
    private JwtUtil jwtUtil;
	@Autowired
    private UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
//        log.info("Received registration request for email: {}", request.getEmail());
        try {
            User user = registerUserUseCase.execute(request.getEmail(), request.getPassword(), request.getName());
            
            String token = jwtUtil.generateToken(user.getEmail(), user.getId());
            
            AuthResponse authResponse = AuthResponse.builder()
                     .token(token)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .name(user.getName())
                    .userId(user.getId())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", authResponse));
        } catch (IllegalArgumentException e) {
//            log.error("Registration failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String token = jwtUtil.generateToken(user.getEmail(), user.getId());
            
            AuthResponse authResponse = AuthResponse.builder()
                     .token(token)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .name(user.getName())
                    .userId(user.getId())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid credentials"));
        }
    }
}
