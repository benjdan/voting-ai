/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityCheckResult {
    
    private boolean passed;
    
    private String checkId;
    
    private String inputHash;
    
    private LocalDateTime checkedAt;
    
    private String lastError;
    
    private String riskLevel;
    
    private int threatCount;
    
    private Map<String, String> detectedThreats;
    
    private String sanitizedInput;
    
    private Map<String, Object> context;
    
    public static SecurityCheckResult passed(String sanitizedInput) {
        return SecurityCheckResult.builder()
            .passed(true)
            .sanitizedInput(sanitizedInput)
            .riskLevel("LOW")
            .threatCount(0)
            .detectedThreats(new HashMap<>())
            .context(new HashMap<>())
            .checkedAt(LocalDateTime.now())
            .build();
    }
    
    public static SecurityCheckResult failed(String error, String riskLevel) {
        return SecurityCheckResult.builder()
            .passed(false)
            .lastError(error)
            .riskLevel(riskLevel)
            .threatCount(1)
            .detectedThreats(new HashMap<>())
            .context(new HashMap<>())
            .checkedAt(LocalDateTime.now())
            .build();
    }
    
    public void addThreat(String threatType, String description) {
        if (this.detectedThreats == null) {
            this.detectedThreats = new HashMap<>();
        }
        this.detectedThreats.put(threatType, description);
        this.threatCount = this.detectedThreats.size();
    }
    
    public void addContext(String key, Object value) {
        if (this.context == null) {
            this.context = new HashMap<>();
        }
        this.context.put(key, value);
    }
    
    public boolean isCriticalThreat() {
        return "CRITICAL".equals(this.riskLevel);
    }
    
    public boolean isHighThreat() {
        return "HIGH".equals(this.riskLevel);
    }
    
    public String getSummary() {
        return String.format(
            "SecurityCheck[passed=%b, riskLevel=%s, threats=%d, error=%s]",
            passed, riskLevel, threatCount, lastError != null ? lastError : "none"
        );
    }
}
