package com.voting.application.usecase;

import com.voting.application.service.BlockchainService;
import com.voting.domain.model.BlockchainRecord;
import com.voting.domain.model.User;
import com.voting.domain.model.Vote;
import com.voting.domain.model.VoteOption;
import com.voting.domain.port.BlockchainRepository;
import com.voting.domain.port.UserRepository;
import com.voting.domain.port.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CastVoteUseCaseTest {
    
    @Mock
    private VoteRepository voteRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private BlockchainService blockchainService;
    
    @Mock
    private BlockchainRepository blockchainRepository;
    
    private CastVoteUseCase castVoteUseCase;
    
    @BeforeEach
    void setUp() {
        castVoteUseCase = new CastVoteUseCase(
                voteRepository, 
                userRepository, 
                blockchainService, 
                blockchainRepository
        );
    }
    
    @Test
    void testExecute_SuccessfulVoteCast() {
        User user = User.builder().id(1L).email("test@example.com").build();
        VoteOption option = VoteOption.builder().id(1L).optionText("Option 1").voteCount(0).build();
        Vote vote = Vote.builder()
                .id(1L)
                .title("Test Vote")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .active(true)
                .options(new ArrayList<>(List.of(option)))
                .build();
        option.setVote(vote);
        
        BlockchainRecord blockchainRecord = BlockchainRecord.builder()
                .id(1L)
                .blockNumber(0L)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));
        when(blockchainRepository.findByUserId(1L)).thenReturn(List.of());
        when(blockchainService.createVoteBlock(user, vote, option)).thenReturn(blockchainRecord);
        when(blockchainRepository.save(blockchainRecord)).thenReturn(blockchainRecord);
        
        BlockchainRecord result = castVoteUseCase.execute(1L, 1L, 1L);
        
        assertNotNull(result);
        assertEquals(1, option.getVoteCount());
        verify(voteRepository).save(vote);
        verify(blockchainRepository).save(blockchainRecord);
    }
    
    @Test
    void testExecute_VoteNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(voteRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            castVoteUseCase.execute(1L, 1L, 1L);
        });
    }
    
    @Test
    void testExecute_VoteNotOpen() {
        User user = User.builder().id(1L).build();
        Vote vote = Vote.builder()
                .id(1L)
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .active(true)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));
        
        assertThrows(IllegalStateException.class, () -> {
            castVoteUseCase.execute(1L, 1L, 1L);
        });
    }
    
    @Test
    void testExecute_UserAlreadyVoted() {
        User user = User.builder().id(1L).build();
        Vote vote = Vote.builder()
                .id(1L)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .active(true)
                .build();
        
        BlockchainRecord existingRecord = BlockchainRecord.builder()
                .vote(vote)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));
        when(blockchainRepository.findByUserId(1L)).thenReturn(List.of(existingRecord));
        
        assertThrows(IllegalStateException.class, () -> {
            castVoteUseCase.execute(1L, 1L, 1L);
        });
    }
}
