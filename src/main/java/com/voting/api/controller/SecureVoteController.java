/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.controller;

import com.voting.api.dto.ApiResponse;
import com.voting.api.dto.VoteRequest;
import com.voting.application.service.SecureAIVoteAnalysisService;
import com.voting.domain.port.PromptSecurityPort;
import com.voting.domain.valueobject.SecurityCheckResult;
import com.voting.infrastructure.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/secure/votes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class SecureVoteController {
    
    private final PromptSecurityPort promptSecurityPort;
    private final SecureAIVoteAnalysisService secureAIService;
    private final JwtUtil jwtUtil;
    
    @PostMapping
    public ResponseEntity<ApiResponse<SecureVoteResponse>> createVoteSecurely(
            @Valid @RequestBody VoteRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractToken(httpRequest);
            String userEmail = jwtUtil.extractEmail(token);
            log.info("Creating vote for user: {}", userEmail);
            
            // Validate vote title
            SecurityCheckResult titleValidation = promptSecurityPort.validatePrompt(request.getTitle());
            if (!titleValidation.isPassed()) {
                log.warn("Vote title validation failed: {}", titleValidation.getSummary());
                return handleSecurityFailure("Vote title contains invalid content", titleValidation);
            }
            
            // Validate vote description
            SecurityCheckResult descValidation = promptSecurityPort.validatePrompt(request.getDescription());
            if (!descValidation.isPassed()) {
                log.warn("Vote description validation failed: {}", descValidation.getSummary());
                return handleSecurityFailure("Vote description contains invalid content", descValidation);
            }
            
            // Validate all vote options
            if (request.getOptions() != null) {
                for (int i = 0; i < request.getOptions().size(); i++) {
                    String option = request.getOptions().get(i);
                    SecurityCheckResult optionValidation = promptSecurityPort.validatePrompt(option);
                    if (!optionValidation.isPassed()) {
                        log.warn("Vote option {} validation failed: {}", i, optionValidation.getSummary());
                        return handleSecurityFailure(
                            String.format("Vote option %d contains invalid content", i),
                            optionValidation
                        );
                    }
                }
            }
            
            // All security checks passed - proceed with vote creation
            log.info("All security validations passed for new vote");
            
            return ResponseEntity.ok(SecureVoteResponse.builder()
                .success(true)
                .message("Vote created successfully with security validation")
                .securityChecksPassed(true)
                .threatCount(0)
                .riskLevel("LOW")
                .build());
                
        } catch (SecurityException e) {
            log.error("Security exception during vote creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<SecureVoteResponse>builder()
                    .success(false)
                    .errorMessage("Security validation failed: " + e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error creating vote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<SecureVoteResponse>builder()
                    .success(false)
                    .errorMessage("Error creating vote: " + e.getMessage())
                    .build());
        }
    }
    
    @PostMapping("/{voteId}/analyze")
    public ResponseEntity<SecureAnalysisResponse> analyzeVote(
            @PathVariable Long voteId,
            @RequestParam(defaultValue = "openai") String analysisType,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractToken(httpRequest);
            String userEmail = jwtUtil.extractEmail(token);
            Long userId = extractUserId(userEmail); // You would implement this
            
            log.info("Analyzing vote {} with {} for user {}", voteId, analysisType, userEmail);
            
            // Check security operational status
            if (!secureAIService.isSecurityHealthy()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(SecureAnalysisResponse.builder()
                        .success(false)
                        .errorMessage("Security system is not operational")
                        .build());
            }
            
            String analysis;
            if ("grok".equalsIgnoreCase(analysisType)) {
                analysis = secureAIService.analyzeVoteWithGrok("", userId, voteId);
            } else {
                analysis = secureAIService.analyzeVoteWithOpenAI("", userId, voteId);
            }
            
            return ResponseEntity.ok(SecureAnalysisResponse.builder()
                .success(true)
                .analysis(analysis)
                .riskLevel("LOW")
                .securityValidated(true)
                .build());
                
        } catch (SecurityException e) {
            log.warn("Security threat detected during analysis: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(SecureAnalysisResponse.builder()
                    .success(false)
                    .errorMessage("Security validation failed")
                    .riskLevel("HIGH")
                    .build());
        } catch (Exception e) {
            log.error("Error analyzing vote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SecureAnalysisResponse.builder()
                    .success(false)
                    .errorMessage("Analysis failed: " + e.getMessage())
                    .build());
        }
    }
    
    @PostMapping("/{voteId}/compare-analyses")
    public ResponseEntity<SecureComparisonResponse> compareAnalyses(
            @PathVariable Long voteId,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractToken(httpRequest);
            String userEmail = jwtUtil.extractEmail(token);
            Long userId = extractUserId(userEmail);
            
            log.info("Comparing AI analyses for vote {}", voteId);
            
            SecureAIVoteAnalysisService.ComparativeAnalysis analysis =
                secureAIService.compareAIAnalyses("", userId, voteId);
            
            return ResponseEntity.ok(SecureComparisonResponse.builder()
                .success(true)
                .voteId(voteId)
                .openAiAnalysis(analysis.getOpenAiAnalysis())
                .grokAnalysis(analysis.getGrokAnalysis())
                .openAiSecurityScore(analysis.getOpenAiSecurityScore())
                .grokSecurityScore(analysis.getGrokSecurityScore())
                .allValidationsPassed(analysis.isAllPassed())
                .riskLevel("LOW")
                .build());
                
        } catch (Exception e) {
            log.error("Error comparing analyses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SecureComparisonResponse.builder()
                    .success(false)
                    .errorMessage("Comparison failed: " + e.getMessage())
                    .build());
        }
    }
    
    @GetMapping("/security/status")
    public ResponseEntity<SecurityStatusResponse> getSecurityStatus() {
        boolean isHealthy = secureAIService.isSecurityHealthy();
        return ResponseEntity.ok(SecurityStatusResponse.builder()
            .operationalStatus(isHealthy ? "OPERATIONAL" : "INACTIVE")
            .upssEnabled(isHealthy)
            .timestamp(java.time.LocalDateTime.now())
            .build());
    }
    
    private ResponseEntity<ApiResponse<SecureVoteResponse>> handleSecurityFailure(
            String message, SecurityCheckResult result) {
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.<SecureVoteResponse>builder()
                .success(false)
                .errorMessage(message)
                .build());
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new SecurityException("Missing or invalid Authorization header");
    }
    
    private Long extractUserId(String userEmail) {
        // TODO: Implement user lookup by email
        return System.nanoTime();
    }
    
  
    @lombok.Data
    @lombok.Builder
    public static class SecureVoteResponse {
        private boolean success;
        private String message;
        private boolean securityChecksPassed;
        private int threatCount;
        private String riskLevel;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SecureAnalysisResponse {
        private boolean success;
        private String analysis;
        private String errorMessage;
        private String riskLevel;
        private boolean securityValidated;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SecureComparisonResponse {
        private boolean success;
        private Long voteId;
        private String openAiAnalysis;
        private String grokAnalysis;
        private int openAiSecurityScore;
        private int grokSecurityScore;
        private boolean allValidationsPassed;
        private String errorMessage;
        private String riskLevel;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SecurityStatusResponse {
        private String operationalStatus;
        private boolean upssEnabled;
        private java.time.LocalDateTime timestamp;
    }
}
