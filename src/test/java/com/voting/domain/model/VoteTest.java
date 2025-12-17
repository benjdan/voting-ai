package com.voting.domain.model;

import org.junit.jupiter.api.Test;

import test.java.com.voting.application.usecase.ExtendWith;
import test.java.com.voting.application.usecase.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VoteTest {
    
    @Test
    void testVoteIsOpen_WhenWithinDateRange() {
        User creator = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();
        
        Vote vote = Vote.builder()
                .title("Test Vote")
                .creator(creator)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .active(true)
                .build();
        
        assertTrue(vote.isOpen());
    }
    
    @Test
    void testVoteIsNotOpen_WhenNotStarted() {
        User creator = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();
        
        Vote vote = Vote.builder()
                .title("Test Vote")
                .creator(creator)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .active(true)
                .build();
        
        assertFalse(vote.isOpen());
    }
    
    @Test
    void testVoteIsNotOpen_WhenExpired() {
        User creator = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();
        
        Vote vote = Vote.builder()
                .title("Test Vote")
                .creator(creator)
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .active(true)
                .build();
        
        assertFalse(vote.isOpen());
    }
    
    @Test
    void testVoteIsNotOpen_WhenInactive() {
        User creator = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();
        
        Vote vote = Vote.builder()
                .title("Test Vote")
                .creator(creator)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .active(false)
                .build();
        
        assertFalse(vote.isOpen());
    }
}
