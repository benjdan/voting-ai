/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.service;

import com.voting.domain.model.BlockchainRecord;
import com.voting.domain.model.User;
import com.voting.domain.model.Vote;
import com.voting.domain.model.VoteOption;
import com.voting.domain.valueobject.VoteRecord;

import java.util.List;

public interface BlockchainService {
    BlockchainRecord createVoteBlock(User user, Vote vote, VoteOption voteOption);
    boolean verifyBlockchain();
    List<VoteRecord> getVoteHistory(Long voteId);
    String calculateHash(Long blockNumber, String previousHash, String data, String nonce);
}
