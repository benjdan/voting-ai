/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.infrastructure.persistence;

import com.voting.domain.port.PromptSecurityPort;
import com.voting.domain.valueobject.SecurityCheckResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@Slf4j
public class UPSSSecurityAdapter implements PromptSecurityPort {
    
    // Configuration properties
    @Value("${security.upss.enabled:true}")
    private boolean upssEnabled;
    
    @Value("${security.upss.max-input-length:32768}")
    private int maxInputLength;
    
    @Value("${security.upss.enable-audit:true}")
    private boolean auditLoggingEnabled;
    
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|ALTER|EXEC|EXECUTE|SCRIPT|OR\\s+1\\s*=\\s*1)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );
    
    private static final Pattern CODE_EXECUTION_PATTERN = Pattern.compile(
        "(?i)(eval|__import__|exec|system|os\\.system|subprocess|Popen|Runtime\\.getRuntime|ProcessBuilder)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );
    
    private static final Pattern SCRIPT_INJECTION_PATTERN = Pattern.compile(
        "(?i)(<script|javascript:|onerror=|onload=|eval\\(|expression\\()",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );
    
    private static final Pattern CMD_EXECUTION_PATTERN = Pattern.compile(
        "(?i)(cmd\\.exe|/bin/bash|/bin/sh|powershell|bash -c)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );
    
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "(\\.\\\\/|\\.\\./|\\\\x2e\\\\x2e|%2e%2e)",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public SecurityCheckResult validatePrompt(String userInput) {
        log.debug("Validating prompt input");
        
        if (!upssEnabled) {
            log.warn("UPSS security is disabled");
            return SecurityCheckResult.passed(userInput);
        }
        
        if (userInput == null || userInput.isEmpty()) {
            return SecurityCheckResult.passed("");
        }
        
        return performSecurityCheck(userInput, "PROMPT", null, null);
    }
    
    @Override
    public SecurityCheckResult validateAIResponse(String aiOutput) {
        log.debug("Validating AI response output");
        
        if (!upssEnabled) {
            return SecurityCheckResult.passed(aiOutput);
        }
        
        if (aiOutput == null || aiOutput.isEmpty()) {
            return SecurityCheckResult.passed("");
        }
        
        return performSecurityCheck(aiOutput, "AI_OUTPUT", null, null);
    }
    
    @Override
    public SecurityCheckResult validateVoteDescription(String voteDescription, Long userId) {
        log.debug("Validating vote description for user: {}", userId);
        
        if (!upssEnabled) {
            return SecurityCheckResult.passed(voteDescription);
        }
        
        if (voteDescription == null || voteDescription.isEmpty()) {
            return SecurityCheckResult.passed("");
        }
        
        SecurityCheckResult result = performSecurityCheck(voteDescription, "VOTE_DESCRIPTION", userId, null);
        
        if (auditLoggingEnabled) {
            auditLog(userId, "VOTE_DESCRIPTION_CHECK", result.isPassed() ? "PASSED" : "FAILED", result.getRiskLevel());
        }
        
        return result;
    }
    
    @Override
    public SecurityCheckResult validateVoteAnalysis(String analysisOutput, Long voteId) {
        log.debug("Validating vote analysis for vote: {}", voteId);
        
        if (!upssEnabled) {
            return SecurityCheckResult.passed(analysisOutput);
        }
        
        if (analysisOutput == null || analysisOutput.isEmpty()) {
            return SecurityCheckResult.passed("");
        }
        
        SecurityCheckResult result = performSecurityCheck(analysisOutput, "VOTE_ANALYSIS", null, voteId);
        
        if (auditLoggingEnabled) {
            auditLog(null, "VOTE_ANALYSIS_CHECK", result.isPassed() ? "PASSED" : "FAILED", result.getRiskLevel());
        }
        
        return result;
    }
    
    @Override
    public boolean isSecurityOperational() {
        return upssEnabled;
    }
    
    private SecurityCheckResult performSecurityCheck(String input, String checkType, Long userId, Long voteId) {
        SecurityCheckResult.SecurityCheckResultBuilder resultBuilder = SecurityCheckResult.builder()
            .checkId(generateCheckId())
            .inputHash(calculateChecksum(input))
            .checkedAt(LocalDateTime.now())
            .sanitizedInput(input)
            .detectedThreats(new HashMap<>())
            .context(new HashMap<>());
        
        // 1. Check input length
        if (input.length() > maxInputLength) {
            String error = String.format("Input exceeds maximum length: %d > %d", input.length(), maxInputLength);
            log.warn("Security check failed - {}", error);
            resultBuilder.passed(false)
                .lastError(error)
                .riskLevel("HIGH")
                .threatCount(1);
            return resultBuilder.build();
        }
        
        // 2. Initialize threat tracking
        Map<String, String> threats = new HashMap<>();
        int threatCount = 0;
        String riskLevel = "LOW";
        boolean hasThreat = false;
        
        // 3. Detect SQL injection
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            threats.put("SQL_INJECTION", "SQL injection pattern detected");
            threatCount++;
            hasThreat = true;
            riskLevel = "HIGH";
            log.warn("SQL injection detected in {}", checkType);
        }
        
        // 4. Detect code execution attempts
        if (CODE_EXECUTION_PATTERN.matcher(input).find()) {
            threats.put("CODE_EXECUTION", "Code execution pattern detected");
            threatCount++;
            hasThreat = true;
            riskLevel = "CRITICAL";
            log.warn("Code execution attempt detected in {}", checkType);
        }
        
        // 5. Detect script injection
        if (SCRIPT_INJECTION_PATTERN.matcher(input).find()) {
            threats.put("SCRIPT_INJECTION", "Script injection pattern detected");
            threatCount++;
            hasThreat = true;
            riskLevel = "HIGH";
            log.warn("Script injection detected in {}", checkType);
        }
        
        // 6. Detect command execution
        if (CMD_EXECUTION_PATTERN.matcher(input).find()) {
            threats.put("COMMAND_EXECUTION", "Command execution pattern detected");
            threatCount++;
            hasThreat = true;
            riskLevel = "CRITICAL";
            log.warn("Command execution attempt detected in {}", checkType);
        }
        
        // 7. Detect path traversal
        if (PATH_TRAVERSAL_PATTERN.matcher(input).find()) {
            threats.put("PATH_TRAVERSAL", "Path traversal pattern detected");
            threatCount++;
            hasThreat = true;
            riskLevel = "HIGH";
            log.warn("Path traversal attempt detected in {}", checkType);
        }
        // TODO: Cover others
        // Build result
        resultBuilder.passed(!hasThreat)
            .threatCount(threatCount)
            .detectedThreats(threats)
            .riskLevel(riskLevel);
        
        if (hasThreat) {
            resultBuilder.lastError(String.format("Security threats detected: %d threat(s)", threatCount));
        }
        
        // Add context
        SecurityCheckResult result = resultBuilder.build();
        result.addContext("checkType", checkType);
        result.addContext("userId", userId);
        result.addContext("voteId", voteId);
        result.addContext("inputLength", input.length());
        
        logSecurityEvent(result, checkType, userId, voteId);
        
        return result;
    }
    
    private String calculateChecksum(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to calculate checksum", e);
            return "ERROR";
        }
    }
    
    private String generateCheckId() {
        return "SC_" + System.nanoTime();
    }
    
    private void logSecurityEvent(SecurityCheckResult result, String checkType, Long userId, Long voteId) {
        if (!auditLoggingEnabled) {
            return;
        }
        
        if (!result.isPassed()) {
            log.warn("SECURITY_EVENT: type={}, passed={}, riskLevel={}, threats={}, userId={}, voteId={}, error={}",
                checkType, result.isPassed(), result.getRiskLevel(), result.getThreatCount(), userId, voteId, result.getLastError());
        } else {
            log.debug("SECURITY_EVENT: type={}, passed={}, riskLevel={}, userId={}, voteId={}",
                checkType, result.isPassed(), result.getRiskLevel(), userId, voteId);
        }
    }
    
    private void auditLog(Long userId, String action, String result, String riskLevel) {
        log.info("AUDIT_LOG: action={}, result={}, riskLevel={}, userId={}, timestamp={}",
            action, result, riskLevel, userId, LocalDateTime.now());
    }
}
