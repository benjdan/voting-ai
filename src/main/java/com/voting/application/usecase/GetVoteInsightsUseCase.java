/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voting.application.service.AIService;

@Service
@AllArgsConstructor
public class GetVoteInsightsUseCase {
    
	@Autowired
    private AIService aiService;
    
    public String execute(Long voteId) {
        return aiService.generateVoteInsights(voteId);
    }
}
