/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import com.voting.application.service.BlockchainService;
import com.voting.domain.valueobject.VoteRecord;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GetVoteHistoryUseCase {
    private static final Logger LOGGER = LogManager.getLogger(GetVoteHistoryUseCase.class);

	@Autowired
    private BlockchainService blockchainService;
    
    public List<VoteRecord> execute(Long voteId) {
		LOGGER.debug("Fetching vote history for voteId={}", voteId);
        return blockchainService.getVoteHistory(voteId);
    }
}
