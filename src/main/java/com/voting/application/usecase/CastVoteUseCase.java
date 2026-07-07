/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import com.voting.application.service.BlockchainService;
import com.voting.domain.model.BlockchainRecord;
import com.voting.domain.model.User;
import com.voting.domain.model.Vote;
import com.voting.domain.model.VoteOption;
import com.voting.domain.port.BlockchainRepository;
import com.voting.domain.port.UserRepository;
import com.voting.domain.port.VoteRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
//@AllArgsConstructor
@NoArgsConstructor
public class CastVoteUseCase {
	private static final Logger LOGGER = LogManager.getLogger(CastVoteUseCase.class);
    
	@Autowired
    private VoteRepository voteRepository;
	@Autowired
    private UserRepository userRepository;
	@Autowired
    private BlockchainService blockchainService;
	@Autowired
    private BlockchainRepository blockchainRepository;
    
    @Transactional
    public BlockchainRecord execute(Long userId, Long voteId, Long voteOptionId) {
		LOGGER.info("Casting vote: userId={}, voteId={}, optionId={}", userId, voteId, voteOptionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        LOGGER.debug("Found user: userId={}, email={}", user.getId(), user.getEmail());
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found"));
        LOGGER.debug("Found vote: voteId={}, title='{}', isOpen={}", vote.getId(), vote.getTitle(), vote.isOpen());

        if (!vote.isOpen()) {
			LOGGER.warn("Attempt to vote on closed poll: voteId={}, userId={}", voteId, userId);
            throw new IllegalStateException("Vote is not open");
        }
        
        boolean hasVoted = blockchainRepository.findByUserId(userId).stream()
                .anyMatch(record -> record.getVote().getId().equals(voteId));
        
        if (hasVoted) {
			LOGGER.warn("User has already voted in this poll: userId={}, voteId={}", userId, voteId);
            throw new IllegalStateException("User has already voted in this poll");
        }
        
        VoteOption voteOption = vote.getOptions().stream()
                .filter(option -> option.getId().equals(voteOptionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Vote option not found"));
        LOGGER.debug("Found vote option: optionId={}, text='{}'", voteOption.getId(), voteOption.getOptionText());
		
        voteOption.incrementVoteCount();
        voteRepository.save(vote);
        
        BlockchainRecord blockchainRecord = blockchainService.createVoteBlock(user, vote, voteOption);
        BlockchainRecord savedRecord = blockchainRepository.save(blockchainRecord);
        
        LOGGER.info("Vote cast successfully: userId={}, voteId={}, optionId={}, blockNumber={}", userId, voteId, voteOptionId, savedRecord.getBlockNumber());
        return savedRecord;
    }
}
