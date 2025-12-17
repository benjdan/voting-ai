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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
//@AllArgsConstructor
@NoArgsConstructor
public class CastVoteUseCase {
    
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found"));
        
        if (!vote.isOpen()) {
            throw new IllegalStateException("Vote is not open");
        }
        
        boolean hasVoted = blockchainRepository.findByUserId(userId).stream()
                .anyMatch(record -> record.getVote().getId().equals(voteId));
        
        if (hasVoted) {
            throw new IllegalStateException("User has already voted in this poll");
        }
        
        VoteOption voteOption = vote.getOptions().stream()
                .filter(option -> option.getId().equals(voteOptionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Vote option not found"));
        
        voteOption.incrementVoteCount();
        voteRepository.save(vote);
        
        BlockchainRecord blockchainRecord = blockchainService.createVoteBlock(user, vote, voteOption);
        return blockchainRepository.save(blockchainRecord);
    }
}
