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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateVoteUseCase {
	private static final Logger LOGGER = LogManager.getLogger(CreateVoteUseCase.class);
    
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
        LOGGER.info("Creating vote with title='{}', creatorId={}", title, creatorId);
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        LOGGER.debug("Found creator user: userId={}, email={}", creator.getId(), creator.getEmail());
        String aiEnhancedDescription = null;
        if (useAIEnhancement && description != null && !description.isEmpty()) {
			LOGGER.debug("AI enhancement enabled for vote title='{}', attempting to enhance description", title);
            try {
                aiEnhancedDescription = aiService.enhanceVoteDescription(title, description);
				LOGGER.info("Successfully enhanced vote description for title='{}'", title);
            } catch (Exception e) {
				LOGGER.error("Failed to enhance vote description for title='{}': {}", title, e.getMessage(), e);
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
        
        Vote savedVote = voteRepository.save(vote);
        LOGGER.info("Vote created successfully with voteId={}, title='{}', options={}", savedVote.getId(), title, optionTexts.size());
        return savedVote;
    }
}
