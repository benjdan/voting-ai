/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import com.voting.application.service.AIService;
import com.voting.domain.model.User;
import com.voting.domain.model.Vote;
import com.voting.domain.model.VoteOption;
import com.voting.domain.port.UserRepository;
import com.voting.domain.port.VoteRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateVoteUseCase {
    
	@Autowired
    private VoteRepository voteRepository;
	@Autowired
    private UserRepository userRepository;
	@Autowired
    private AIService aiService;
    
    @Transactional
    public Vote execute(Long creatorId, String title, String description, 
                       List<String> optionTexts, LocalDateTime startDate, LocalDateTime endDate,
                       boolean useAIEnhancement) {
        
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        
        String aiEnhancedDescription = null;
        if (useAIEnhancement && description != null && !description.isEmpty()) {
            try {
                aiEnhancedDescription = aiService.enhanceVoteDescription(title, description);
            } catch (Exception e) {
                aiEnhancedDescription = null;
            }
        }
        
        Vote vote = Vote.builder()
                .title(title)
                .description(description)
                .aiEnhancedDescription(aiEnhancedDescription)
                .creator(creator)
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .build();
        
        for (String optionText : optionTexts) {
            VoteOption option = VoteOption.builder()
                    .vote(vote)
                    .optionText(optionText)
                    .voteCount(0)
                    .build();
            vote.getOptions().add(option);
        }
        
        return voteRepository.save(vote);
    }
}
