/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.port;

import com.voting.domain.model.BlockchainRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaBlockchainRepository extends JpaRepository<BlockchainRecord, Long> {
    
    @Query("SELECT b FROM BlockchainRecord b ORDER BY b.blockNumber DESC LIMIT 1")
    Optional<BlockchainRecord> findLatestBlock();
    
    List<BlockchainRecord> findByUserId(Long userId);
    
    List<BlockchainRecord> findByVoteId(Long voteId);
}
