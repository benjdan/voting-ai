/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import com.voting.application.service.BlockchainService;
import com.voting.domain.valueobject.VoteRecord;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GetVoteHistoryUseCase {
    
	@Autowired
    private BlockchainService blockchainService;
    
    public List<VoteRecord> execute(Long voteId) {
        return blockchainService.getVoteHistory(voteId);
    }
}
