/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.port;

import com.voting.domain.model.BlockchainRecord;
import com.voting.domain.model.Vote;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlockchainRepository {
    BlockchainRecord save(BlockchainRecord record);
    Optional<BlockchainRecord> findLatestBlock();
    List<BlockchainRecord> findByUserId(Long userId);
    @Query("SELECT v FROM BlockchainRecord v WHERE v.id = :voteId")
    List<BlockchainRecord> findByVoteId(Long voteId);
//    @Query("SELECT v FROM BlockchainRecord v")
    List<BlockchainRecord> findAll();
    long count();
}
