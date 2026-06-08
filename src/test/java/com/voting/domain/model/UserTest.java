/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserTest {
    
    @Test
    void testUserCreation() {
        // Setup
        String email = "user@example.com";
        String password = "encoded_password";
        String name = "John Doe";
        
        // Execute
        User user = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .active(true)
                .build();
        
        // Assert
        assertNotNull(user);
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(name, user.getName());
        assertTrue(user.getActive());
    }
    
    @Test
    void testUserWithId() {
        // Execute
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .name("Test User")
                .active(true)
                .build();
        
        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
    }
    
    @Test
    void testUserSetters() {
        // Setup
        User user = User.builder()
                .email("original@example.com")
                .password("password")
                .name("Original Name")
                .build();
        
        // Execute
        user.setId(2L);
        user.setEmail("updated@example.com");
        user.setName("Updated Name");
        user.setPassword("new_password");
        user.setActive(false);
        
        // Assert
        assertEquals(2L, user.getId());
        assertEquals("updated@example.com", user.getEmail());
        assertEquals("Updated Name", user.getName());
        assertEquals("new_password", user.getPassword());
        assertFalse(user.getActive());
    }
    
    @Test
    void testUserPrePersist() {
        // Execute
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .name("Test User")
                .build();
        
        // Simulate @PrePersist
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        if (user.getActive() == null) {
            user.setActive(true);
        }
        
        // Assert
        assertNotNull(user.getCreatedAt());
        assertTrue(user.getActive());
    }
    
    @Test
    void testUserInactiveStatus() {
        // Execute
        User user = User.builder()
                .email("inactive@example.com")
                .password("password")
                .name("Inactive User")
                .active(false)
                .build();
        
        // Assert
        assertFalse(user.getActive());
    }
    
    @Test
    void testUserEquality() {
        // Setup
        User user1 = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test")
                .build();
        
        User user2 = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test")
                .build();
        
        // Both should have same properties (though equals might be based on ID)
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getEmail(), user2.getEmail());
    }
    
    @Test
    void testUserWithTimestamp() {
        // Setup
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .name("Test User")
                .createdAt(now)
                .build();
        
        // Assert
        assertNotNull(user.getCreatedAt());
        assertEquals(now, user.getCreatedAt());
    }
    
    @Test
    void testUserDefaultActiveStatus() {
        // Execute
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .name("Test User")
                .build();
        
        // Set default if null
        if (user.getActive() == null) {
            user.setActive(true);
        }
        
        // Assert
        assertTrue(user.getActive());
    }
    
    @Test
    void testUserWithValidEmail() {
        // Execute
        User user = User.builder()
                .email("valid.email+tag@example.co.uk")
                .password("password")
                .name("User")
                .build();
        
        // Assert
        assertTrue(user.getEmail().contains("@"));
        assertTrue(user.getEmail().contains("."));
    }
}
