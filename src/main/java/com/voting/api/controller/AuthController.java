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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class AuthController {
    private static final Logger LOGGER = LogManager.getLogger(AuthController.class);
	
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
		LOGGER.info("Received registration request for email: {}", request.getEmail());
        try {
            User user = registerUserUseCase.execute(request.getEmail(), request.getPassword(), request.getName());

			LOGGER.debug("Generating JWT token for userId={}, email={}", user.getId(), user.getEmail());
            String token = jwtUtil.generateToken(user.getEmail(), user.getId());
            
            AuthResponse authResponse = AuthResponse.builder()
                     .token(token)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .name(user.getName())
                    .userId(user.getId())
                    .build();
            LOGGER.info("User registered successfully with email={}, userId={}", user.getEmail(), user.getId());
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", authResponse));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Registration failed for email={}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Unexpected error during registration for email={}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		LOGGER.info("Received login request for email: {}", request.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            LOGGER.debug("Authentication successful for email={}", request.getEmail());
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
            LOGGER.info("User login successful for email={}, userId={}", user.getEmail(), user.getId());
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
        } catch (RuntimeException e) {
            LOGGER.error("User not found during login attempt for email={}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid credentials"));
        } catch (Exception e) {
            LOGGER.error("Authentication failed for email={}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid credentials"));
        }
    }
}
