/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import com.voting.domain.model.User;
import com.voting.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {
    private static final Logger LOGGER = LogManager.getLogger(RegisterUserUseCase.class);

	@Autowired
    private UserRepository userRepository;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
    
    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Transactional
    public User execute(String email, String password, String name) {
		LOGGER.debug("Registering user email={}", email);
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        User user = User.builder()
                .email(email)
                .name(name)
                .active(true)
                .build();
        
        return userRepository.save(user);
    }
}
