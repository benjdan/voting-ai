/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.controller;

import com.voting.api.dto.ApiResponse;
import com.voting.application.usecase.GetVoteInsightsUseCase;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIController {
    
	@Autowired
    private GetVoteInsightsUseCase getVoteInsightsUseCase;
    
    @GetMapping("/insights/{voteId}")
    public ResponseEntity<ApiResponse<String>> getVoteInsights(@PathVariable Long voteId) {
        try {
            String insights = getVoteInsightsUseCase.execute(voteId);
            return ResponseEntity.ok(ApiResponse.success("AI insights generated", insights));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
