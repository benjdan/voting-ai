package com.voting.application.usecase;

import com.voting.domain.model.User;
import com.voting.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    private RegisterUserUseCase registerUserUseCase;
    
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        registerUserUseCase = new RegisterUserUseCase(userRepository);
    }
    
    @Test
    void testExecute_SuccessfulRegistration() {
        String email = "test@example.com";
        String password = "password123";
        String name = "Test User";
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        User result = registerUserUseCase.execute(email, password, name);
        
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(name, result.getName());
        assertTrue(result.getActive());
        
        verify(userRepository).existsByEmail(email);
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testExecute_EmailAlreadyExists() {
        String email = "existing@example.com";
        
        when(userRepository.existsByEmail(email)).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> {
            registerUserUseCase.execute(email, "password", "Name");
        });
        
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }
}
