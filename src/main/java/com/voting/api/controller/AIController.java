/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.controller;

import com.voting.api.dto.ApiResponse;
import com.voting.application.usecase.GetVoteInsightsUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIController {
	private static final Logger LOGGER = LogManager.getLogger(AIController.class);
    
	@Autowired
    private GetVoteInsightsUseCase getVoteInsightsUseCase;
    
    @GetMapping("/insights/{voteId}")
    public ResponseEntity<ApiResponse<String>> getVoteInsights(@PathVariable Long voteId) {
        try {
			LOGGER.info("Received request to generate AI insights for voteId={}", voteId);
            LOGGER.debug("Generating AI insights for voteId={}", voteId);

            String insights = getVoteInsightsUseCase.execute(voteId);

			LOGGER.info("AI insights generated successfully for voteId={}", voteId);
            return ResponseEntity.ok(ApiResponse.success("AI insights generated", insights));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Vote not found for voteId={}: {}", voteId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Error generating AI insights for voteId={}", voteId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
