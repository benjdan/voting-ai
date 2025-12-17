/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.service;

import com.voting.domain.model.Vote;

public interface AIService {
    String enhanceVoteDescription(String title, String description);
    String analyzeVoteResults(Vote vote);
    String generateVoteInsights(Long voteId);
}
