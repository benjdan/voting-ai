/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.port;

import com.voting.domain.valueobject.SecurityCheckResult;

public interface PromptSecurityPort {
   
    SecurityCheckResult validatePrompt(String userInput);
   
    SecurityCheckResult validateAIResponse(String aiOutput);
   
    SecurityCheckResult validateVoteDescription(String voteDescription, Long userId);
   
    SecurityCheckResult validateVoteAnalysis(String analysisOutput, Long voteId);
   
    boolean isSecurityOperational();
}
