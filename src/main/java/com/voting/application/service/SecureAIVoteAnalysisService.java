/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.service;

import com.voting.domain.port.PromptSecurityPort;
import com.voting.domain.valueobject.SecurityCheckResult;
import com.voting.infrastructure.ai.OpenAIServiceImpl;
import com.voting.infrastructure.ai.XAIServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecureAIVoteAnalysisService {
    
    private final PromptSecurityPort promptSecurityPort;
    private final OpenAIServiceImpl openAiService;
    private final XAIServiceImpl xAiService;
    
    public String analyzeVoteWithOpenAI(String voteDescription, Long userId, Long voteId) {
        log.info("Analyzing vote {} for user {} with OpenAI", voteId, userId);
        
        // Step 1: Validate user input
        SecurityCheckResult validationResult = promptSecurityPort.validateVoteDescription(voteDescription, userId);
        
        if (!validationResult.isPassed()) {
            log.warn("Vote description validation failed. Risk: {}, Threats: {}",
                validationResult.getRiskLevel(), validationResult.getThreatCount());
            
            if (validationResult.isCriticalThreat()) {
                throw new SecurityException(
                    String.format("Critical security threat detected: %s", validationResult.getLastError())
                );
            }
            
            if (validationResult.isHighThreat()) {
                log.warn("HIGH threat detected but proceeding with caution: {}", validationResult.getLastError());
            }
        }
        
        // Step 2: Use sanitized input if available
        String inputToProcess = validationResult.getSanitizedInput() != null
            ? validationResult.getSanitizedInput()
            : voteDescription;
        
        // Step 3: Send to AI service
        String aiResponse = openAiService.enhanceVoteDescription(inputToProcess);
        
        // Step 4: Validate AI response
        SecurityCheckResult responseValidation = promptSecurityPort.validateVoteAnalysis(aiResponse, voteId);
        
        if (!responseValidation.isPassed()) {
            log.error("AI response validation failed. Risk: {}", responseValidation.getRiskLevel());
            
            if (responseValidation.isCriticalThreat()) {
                throw new SecurityException("AI response contains security threats");
            }
        }
        
        // Step 5: Return validated response
        return responseValidation.getSanitizedInput();
    }
    
    public String analyzeVoteWithGrok(String voteDescription, Long userId, Long voteId) {
        log.info("Analyzing vote {} for user {} with xAI Grok", voteId, userId);
        
        // Validate input
        SecurityCheckResult validationResult = promptSecurityPort.validateVoteDescription(voteDescription, userId);
        
        if (!validationResult.isPassed()) {
            if (validationResult.isCriticalThreat()) {
                throw new SecurityException("Critical security threat detected");
            }
            log.warn("Security warning during input validation: {}", validationResult.getSummary());
        }
        
        String inputToProcess = validationResult.getSanitizedInput() != null
            ? validationResult.getSanitizedInput()
            : voteDescription;
        
        // Send to xAI service
        String grokResponse = xAiService.analyzeVote(inputToProcess);
        
        // Validate response
        SecurityCheckResult responseValidation = promptSecurityPort.validateVoteAnalysis(grokResponse, voteId);
        
        if (!responseValidation.isPassed()) {
            log.error("Grok response validation failed: {}", responseValidation.getSummary());
            if (responseValidation.isCriticalThreat()) {
                throw new SecurityException("Grok response contains security threats");
            }
        }
        
        return responseValidation.getSanitizedInput();
    }
    
    public ComparativeAnalysis compareAIAnalyses(String voteDescription, Long userId, Long voteId) {
        log.info("Comparing AI analyses for vote {} from user {}", voteId, userId);
        
        SecurityCheckResult validationResult = promptSecurityPort.validateVoteDescription(voteDescription, userId);
        
        if (!validationResult.isPassed() && validationResult.isCriticalThreat()) {
            throw new SecurityException("Input contains critical security threats");
        }
        
        String inputToProcess = validationResult.getSanitizedInput() != null
            ? validationResult.getSanitizedInput()
            : voteDescription;
        
        String openAiAnalysis = openAiService.enhanceVoteDescription(inputToProcess);
        String grokAnalysis = xAiService.analyzeVote(inputToProcess);
        
        SecurityCheckResult openAiValidation = promptSecurityPort.validateVoteAnalysis(openAiAnalysis, voteId);
        SecurityCheckResult grokValidation = promptSecurityPort.validateVoteAnalysis(grokAnalysis, voteId);
        
        return ComparativeAnalysis.builder()
            .voteId(voteId)
            .openAiAnalysis(openAiValidation.getSanitizedInput())
            .grokAnalysis(grokValidation.getSanitizedInput())
            .openAiSecurityScore(calculateSecurityScore(openAiValidation))
            .grokSecurityScore(calculateSecurityScore(grokValidation))
            .allPassed(openAiValidation.isPassed() && grokValidation.isPassed())
            .build();
    }
    
    public boolean isSecurityHealthy() {
        return promptSecurityPort.isSecurityOperational();
    }
    
    private int calculateSecurityScore(SecurityCheckResult result) {
        if (result.isPassed()) {
            return 100 - (result.getThreatCount() * 10); // Deduct 10 points per threat detected
        } else {
            int score = 50; // Base score for failed validation
            if (result.isCriticalThreat()) {
                score -= 30;
            } else if (result.isHighThreat()) {
                score -= 20;
            }
            return Math.max(0, score);
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ComparativeAnalysis {
        private Long voteId;
        private String openAiAnalysis;
        private String grokAnalysis;
        private int openAiSecurityScore;
        private int grokSecurityScore;
        private boolean allPassed;
    }
}
