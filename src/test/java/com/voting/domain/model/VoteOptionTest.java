package com.voting.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VoteOptionTest {
    
    @Test
    void testIncrementVoteCount() {
        VoteOption option = VoteOption.builder()
                .optionText("Option 1")
                .voteCount(0)
                .build();
        
        assertEquals(0, option.getVoteCount());
        
        option.incrementVoteCount();
        assertEquals(1, option.getVoteCount());
        
        option.incrementVoteCount();
        assertEquals(2, option.getVoteCount());
    }
    
    @Test
    void testDefaultVoteCountIsZero() {
        VoteOption option = VoteOption.builder()
                .optionText("Option 1")
                .build();
        
        assertEquals(0, option.getVoteCount());
    }
}
