/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.infrastructure.persistence;

import com.voting.domain.model.BlockchainRecord;
import com.voting.domain.port.BlockchainRepository;
import com.voting.domain.port.JpaBlockchainRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class BlockchainRepositoryAdapter implements BlockchainRepository {
    
	@Autowired
    private JpaBlockchainRepository jpaBlockchainRepository;
    
    @Override
    public BlockchainRecord save(BlockchainRecord record) {
        return jpaBlockchainRepository.save(record);
    }
    
    @Override
    public Optional<BlockchainRecord> findLatestBlock() {
        return jpaBlockchainRepository.findLatestBlock();
    }
    
    @Override
    public List<BlockchainRecord> findByUserId(Long userId) {
        return jpaBlockchainRepository.findByUserId(userId);
    }
    
    @Override
    public List<BlockchainRecord> findByVoteId(Long voteId) {
        return jpaBlockchainRepository.findByVoteId(voteId);
    }
    
    @Override
    public List<BlockchainRecord> findAll() {
        return jpaBlockchainRepository.findAll();
    }
    
    @Override
    public long count() {
        return jpaBlockchainRepository.count();
    }
}
