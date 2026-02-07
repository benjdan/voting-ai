/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import com.voting.application.service.BlockchainService;
import com.voting.domain.valueobject.VoteRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetVoteHistoryUseCaseTest {
    
    @Mock
    private BlockchainService blockchainService;
    
    private GetVoteHistoryUseCase getVoteHistoryUseCase;
    
    @BeforeEach
    void setUp() {
        getVoteHistoryUseCase = new GetVoteHistoryUseCase(blockchainService);
    }
    
    @Test
    void testExecute_GetVoteHistorySuccess() {
        Long voteId = 1L;
        VoteRecord record1 = VoteRecord.builder()
                .userId(1L)
                .userName("User 1")
                .optionText("Option A")
                .timestamp(LocalDateTime.now().minusHours(2))
                .build();
        
        VoteRecord record2 = VoteRecord.builder()
                .userId(2L)
                .userName("User 2")
                .optionText("Option B")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();
        
        VoteRecord record3 = VoteRecord.builder()
                .userId(3L)
                .userName("User 3")
                .optionText("Option A")
                .timestamp(LocalDateTime.now())
                .build();
        
        List<VoteRecord> expectedHistory = List.of(record1, record2, record3);
        
        when(blockchainService.getVoteHistory(voteId)).thenReturn(expectedHistory);
        
        List<VoteRecord> result = getVoteHistoryUseCase.execute(voteId);
        
        assertNotNull(result);
        assertEquals(3, result.size());
//        assertEquals("User 1", result.get(0).getUserName());
        assertEquals("Option B", result.get(1).getOptionText());
        verify(blockchainService).getVoteHistory(voteId);
    }
    
    @Test
    void testExecute_EmptyVoteHistory() {
        Long voteId = 1L;
        
        when(blockchainService.getVoteHistory(voteId)).thenReturn(new ArrayList<>());
        
        List<VoteRecord> result = getVoteHistoryUseCase.execute(voteId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blockchainService).getVoteHistory(voteId);
    }
    
    @Test
    void testExecute_VoteHistoryWithManyRecords() {
        Long voteId = 1L;
        List<VoteRecord> expectedHistory = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            VoteRecord record = VoteRecord.builder()
                    .id((long) i)
                    .userName("User " + i)
                    .optionText("Option " + (i % 3))
                    .timestamp(LocalDateTime.now().minusMinutes(i))
                    .build();
            expectedHistory.add(record);
        }
        
        when(blockchainService.getVoteHistory(voteId)).thenReturn(expectedHistory);
        
        List<VoteRecord> result = getVoteHistoryUseCase.execute(voteId);
        
        assertNotNull(result);
        assertEquals(100, result.size());
        verify(blockchainService).getVoteHistory(voteId);
    }
    
    @Test
    void testExecute_VoteHistoryOrdering() {
        Long voteId = 1L;
        VoteRecord oldRecord = VoteRecord.builder()
                .userId(1L)
                .userName("User 1")
                .optionText("Option A")
                .timestamp(LocalDateTime.now().minusHours(3))
                .build();
        
        VoteRecord newRecord = VoteRecord.builder()
                .userId(2L)
                .userName("User 2")
                .optionText("Option B")
                .timestamp(LocalDateTime.now())
                .build();
        
        List<VoteRecord> expectedHistory = List.of(oldRecord, newRecord);
        
        when(blockchainService.getVoteHistory(voteId)).thenReturn(expectedHistory);
        
        List<VoteRecord> result = getVoteHistoryUseCase.execute(voteId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getTimestamp().isBefore(result.get(1).getTimestamp()));
    }
    
    @Test
    void testExecute_VoteHistoryWithNullVoteId() {
        when(blockchainService.getVoteHistory(null))
                .thenThrow(new IllegalArgumentException("Vote ID cannot be null"));
        
        assertThrows(IllegalArgumentException.class, () ->
                getVoteHistoryUseCase.execute(null)
        );
    }
}
