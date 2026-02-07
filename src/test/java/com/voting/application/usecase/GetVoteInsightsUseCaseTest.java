/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import com.voting.application.service.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetVoteInsightsUseCaseTest {
    
    @Mock
    private AIService aiService;
    
    private GetVoteInsightsUseCase getVoteInsightsUseCase;
    
    @BeforeEach
    void setUp() {
        getVoteInsightsUseCase = new GetVoteInsightsUseCase(aiService);
    }
    
    @Test
    void testExecute_GetInsightsSuccess() {
        Long voteId = 1L;
        String expectedInsights = "Analysis shows Option A leading with 65% votes. "
                + "Trend analysis indicates increasing support over the last 6 hours.";
        
        when(aiService.generateVoteInsights(voteId)).thenReturn(expectedInsights);
        
        String result = getVoteInsightsUseCase.execute(voteId);
        
        assertNotNull(result);
        assertEquals(expectedInsights, result);
        assertTrue(result.contains("65%"));
        assertTrue(result.contains("Option A"));
        verify(aiService).generateVoteInsights(voteId);
    }
    
    @Test
    void testExecute_GetInsightsWithDemographics() {
        Long voteId = 2L;
        String insightsWithDemographics = "Age group 18-25: 72% favor Option A. "
                + "Age group 26-40: 58% favor Option B. "
                + "Geographic hotspot: Eastern region shows 80% support for Option C.";
        
        when(aiService.generateVoteInsights(voteId)).thenReturn(insightsWithDemographics);
        
        String result = getVoteInsightsUseCase.execute(voteId);
        
        assertNotNull(result);
        assertTrue(result.contains("Age group"));
        assertTrue(result.contains("Geographic hotspot"));
        verify(aiService).generateVoteInsights(voteId);
    }
    
    @Test
    void testExecute_GetInsightsWithPredictions() {
        Long voteId = 3L;
        String predictiveInsights = "Current trend analysis suggests Option A will maintain lead. "
                + "Projected final result: Option A 62%, Option B 28%, Option C 10%. "
                + "Confidence level: 87%.";
        
        when(aiService.generateVoteInsights(voteId)).thenReturn(predictiveInsights);
        
        String result = getVoteInsightsUseCase.execute(voteId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Projected"));
        assertTrue(result.contains("87%"));
        verify(aiService).generateVoteInsights(voteId);
    }
    
    @Test
    void testExecute_VoteNotFound() {
        Long voteId = 999L;
        
        when(aiService.generateVoteInsights(voteId))
                .thenThrow(new IllegalArgumentException("Vote not found"));
        
        assertThrows(IllegalArgumentException.class, () ->
                getVoteInsightsUseCase.execute(voteId)
        );
    }
    
    @Test
    void testExecute_AIServiceError() {
        Long voteId = 1L;
        
        when(aiService.generateVoteInsights(voteId))
                .thenThrow(new RuntimeException("AI service unavailable"));
        
        assertThrows(RuntimeException.class, () ->
                getVoteInsightsUseCase.execute(voteId)
        );
    }
    
    @Test
    void testExecute_InsightsWithSentimentAnalysis() {
        Long voteId = 4L;
        String sentimentInsights = "Sentiment Analysis: "
                + "Positive comments: 78%, Neutral: 15%, Negative: 7%. "
                + "Dominant themes: Efficiency, Innovation, Sustainability.";
        
        when(aiService.generateVoteInsights(voteId)).thenReturn(sentimentInsights);
        
        String result = getVoteInsightsUseCase.execute(voteId);
        
        assertNotNull(result);
        assertTrue(result.contains("Sentiment Analysis"));
        assertTrue(result.contains("78%"));
        assertTrue(result.contains("Efficiency"));
    }
    
    @Test
    void testExecute_EmptyInsights() {
        Long voteId = 5L;
        String emptyInsights = "";
        
        when(aiService.generateVoteInsights(voteId)).thenReturn(emptyInsights);
        
        String result = getVoteInsightsUseCase.execute(voteId);
        
        assertNotNull(result);
        assertEquals("", result);
        verify(aiService).generateVoteInsights(voteId);
    }
    
    @Test
    void testExecute_NullVoteId() {
        when(aiService.generateVoteInsights(null))
                .thenThrow(new IllegalArgumentException("Vote ID cannot be null"));
        
        assertThrows(IllegalArgumentException.class, () ->
                getVoteInsightsUseCase.execute(null)
        );
    }
    
    @Test
    void testExecute_LongAnalysisResult() {
        Long voteId = 6L;
        StringBuilder longAnalysis = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longAnalysis.append("Insight point ").append(i).append(": Some detailed analysis. ");
        }
        String longInsights = longAnalysis.toString();
        
        when(aiService.generateVoteInsights(voteId)).thenReturn(longInsights);
        
        String result = getVoteInsightsUseCase.execute(voteId);
        
        assertNotNull(result);
        assertTrue(result.length() > 500);
        assertTrue(result.contains("Insight point 0"));
        assertTrue(result.contains("Insight point 49"));
    }
}
