/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.controller;

import com.voting.api.dto.ApiResponse;
import com.voting.application.usecase.GetVoteInsightsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIControllerTest {
    
    @Mock
    private GetVoteInsightsUseCase getVoteInsightsUseCase;
    
    private AIController aiController;
    
    @BeforeEach
    void setUp() {
        aiController = new AIController(getVoteInsightsUseCase);
    }
    
    @Test
    void testGetVoteInsights_Success() {
        Long voteId = 1L;
        String expectedInsights = "AI analysis shows or indicates positive reception.";
        
        when(getVoteInsightsUseCase.execute(voteId)).thenReturn(expectedInsights);
        
        ResponseEntity<ApiResponse<String>> response = aiController.getVoteInsights(voteId);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(expectedInsights, response.getBody().getData());
        verify(getVoteInsightsUseCase).execute(voteId);
    }
    
    @Test
    void testGetVoteInsights_VoteNotFound() {
        Long voteId = 999L;
        String errorMessage = "Vote not found";
        
        when(getVoteInsightsUseCase.execute(voteId)).thenThrow(new IllegalArgumentException(errorMessage));
        
        ResponseEntity<ApiResponse<String>> response = aiController.getVoteInsights(voteId);
        
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains(errorMessage));
    }
    
    @Test
    void testGetVoteInsights_AIServiceError() {
        Long voteId = 1L;
        String errorMessage = "AI Service temporarily unavailable";
        
        when(getVoteInsightsUseCase.execute(voteId))
                .thenThrow(new RuntimeException(errorMessage));
        
        ResponseEntity<ApiResponse<String>> response = aiController.getVoteInsights(voteId);
        
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().getMessage().contains(errorMessage));
    }
    
    @Test
    void testGetVoteInsights_WithComplexAnalysis() {
        Long voteId = 2L;
        String complexInsights = "Predictive analysis: Option 2 trending up 15% in last hour. "
                + "Network analysis shows influencers favoring Option 3. "
                + "Time-series forecast suggests tight race between options.";
        
        when(getVoteInsightsUseCase.execute(voteId)).thenReturn(complexInsights);
        
        ResponseEntity<ApiResponse<String>> response = aiController.getVoteInsights(voteId);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().contains("Predictive analysis"));
        assertTrue(response.getBody().getData().contains("Network analysis"));
    }
    
    @Test
    void testGetVoteInsights_NullVoteId() {
        when(getVoteInsightsUseCase.execute(null))
                .thenThrow(new IllegalArgumentException("Vote ID cannot be null"));
        
        ResponseEntity<ApiResponse<String>> response = aiController.getVoteInsights(null);
        
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
    }
}
