/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voting.application.service.AIService;

@Service
@AllArgsConstructor
public class GetVoteInsightsUseCase {
    private static final Logger LOGGER = LogManager.getLogger(GetVoteInsightsUseCase.class);

	@Autowired
    private AIService aiService;
    
    public String execute(Long voteId) {
		LOGGER.debug("Generating vote insights for voteId={}", voteId);
        return aiService.generateVoteInsights(voteId);
    }
}
