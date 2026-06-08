/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BlockchainRecordTest {
    
    @Test
    void testBlockchainRecordCreation() {
        // Setup
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("Test User")
                .build();
        
        User creator = User.builder()
                .id(2L)
                .email("creator@example.com")
                .name("Vote Creator")
                .build();
        
        Vote vote = Vote.builder()
                .id(1L)
                .title("Test Vote")
                .creator(creator)
                .build();
        
        VoteOption option = VoteOption.builder()
                .id(1L)
                .optionText("Option A")
                .voteCount(0)
                .build();
        
        // Execute
        BlockchainRecord record = BlockchainRecord.builder()
                .blockNumber(0L)
                .previousHash("0000000000000000000000000000000000000000000000000000000000000000")
                .currentHash("abc123def456")
                .user(user)
                .vote(vote)
                .voteOption(option)
                .build();
        
        // Assert
        assertNotNull(record);
        assertEquals(0L, record.getBlockNumber());
        assertEquals("abc123def456", record.getCurrentHash());
        assertEquals(user, record.getUser());
        assertEquals(vote, record.getVote());
        assertEquals(option, record.getVoteOption());
    }
    
    @Test
    void testBlockchainRecordWithId() {
        // Execute
        BlockchainRecord record = BlockchainRecord.builder()
                .id(1L)
                .blockNumber(1L)
                .currentHash("hash123")
                .build();
        
        // Assert
        assertEquals(1L, record.getId());
    }
    
    @Test
    void testBlockchainRecordSetters() {
        // Setup
        User user = User.builder().id(1L).email("test@example.com").build();
        Vote vote = Vote.builder().id(1L).title("Vote").build();
        VoteOption option = VoteOption.builder().id(1L).optionText("Option").build();
        
        BlockchainRecord record = BlockchainRecord.builder()
                .blockNumber(0L)
                .currentHash("initial")
                .build();
        
        // Execute
        record.setId(2L);
        record.setBlockNumber(5L);
        record.setCurrentHash("updated_hash");
        record.setPreviousHash("previous_hash");
        record.setUser(user);
        record.setVote(vote);
        record.setVoteOption(option);
        
        // Assert
        assertEquals(2L, record.getId());
        assertEquals(5L, record.getBlockNumber());
        assertEquals("updated_hash", record.getCurrentHash());
        assertEquals("previous_hash", record.getPreviousHash());
        assertEquals(user, record.getUser());
        assertEquals(vote, record.getVote());
        assertEquals(option, record.getVoteOption());
    }
    
    @Test
    void testBlockchainRecordGenesisBlock() {
        // Setup - Genesis block has block number 0 and special previous hash
        String genesisHash = "0000000000000000000000000000000000000000000000000000000000000000";
        
        BlockchainRecord genesisBlock = BlockchainRecord.builder()
                .blockNumber(0L)
                .previousHash(genesisHash)
                .currentHash("genesis_current_hash")
                .build();
        
        // Assert
        assertEquals(0L, genesisBlock.getBlockNumber());
        assertEquals(genesisHash, genesisBlock.getPreviousHash());
        assertNotEquals(genesisHash, genesisBlock.getCurrentHash());
    }
    
    @Test
    void testBlockchainRecordChainedBlocks() {
        // Setup
        String block1Hash = "hash_block_1";
        String block2Hash = "hash_block_2";
        String block3Hash = "hash_block_3";
        
        BlockchainRecord block1 = BlockchainRecord.builder()
                .blockNumber(1L)
                .previousHash("0000000000000000000000000000000000000000000000000000000000000000")
                .currentHash(block1Hash)
                .build();
        
        BlockchainRecord block2 = BlockchainRecord.builder()
                .blockNumber(2L)
                .previousHash(block1Hash)
                .currentHash(block2Hash)
                .build();
        
        BlockchainRecord block3 = BlockchainRecord.builder()
                .blockNumber(3L)
                .previousHash(block2Hash)
                .currentHash(block3Hash)
                .build();
        
        // Assert - Verify chain integrity
        assertEquals(block1Hash, block2.getPreviousHash());
        assertEquals(block2Hash, block3.getPreviousHash());
        assertEquals(3L, block3.getBlockNumber());
    }
    
    @Test
    void testBlockchainRecordWithTimestamp() {
        // Setup
        LocalDateTime now = LocalDateTime.now();
        
        BlockchainRecord record = BlockchainRecord.builder()
                .blockNumber(1L)
                .currentHash("hash")
                .timestamp(now)
                .build();
        
        // Assert
        assertNotNull(record.getTimestamp());
        assertEquals(now, record.getTimestamp());
    }
    
    @Test
    void testBlockchainRecordHashConsistency() {
        // Setup
        String consistentHash = "abcdef123456789";
        
        BlockchainRecord record1 = BlockchainRecord.builder()
                .blockNumber(1L)
                .currentHash(consistentHash)
                .build();
        
        BlockchainRecord record2 = BlockchainRecord.builder()
                .blockNumber(1L)
                .currentHash(consistentHash)
                .build();
        
        // Assert - Same block should have same hash
        assertEquals(record1.getCurrentHash(), record2.getCurrentHash());
    }
    
    @Test
    void testBlockchainRecordDifferentHashes() {
        // Setup
        String hash1 = "abc123";
        String hash2 = "xyz789";
        
        BlockchainRecord record1 = BlockchainRecord.builder()
                .blockNumber(1L)
                .currentHash(hash1)
                .build();
        
        BlockchainRecord record2 = BlockchainRecord.builder()
                .blockNumber(2L)
                .currentHash(hash2)
                .build();
        
        // Assert - Different blocks should have different hashes
        assertNotEquals(record1.getCurrentHash(), record2.getCurrentHash());
    }
    
    @Test
    void testBlockchainRecordLongHash() {
        // Setup
        String longHash = "a".repeat(64); // 64 character hash (common for SHA-256)
        
        BlockchainRecord record = BlockchainRecord.builder()
                .blockNumber(1L)
                .currentHash(longHash)
                .build();
        
        // Assert
        assertEquals(64, record.getCurrentHash().length());
    }
    
    @Test
    void testBlockchainRecordSequentialBlockNumbers() {
        // Setup
        for (int i = 0; i < 10; i++) {
            BlockchainRecord record = BlockchainRecord.builder()
                    .blockNumber((long) i)
                    .currentHash("hash_" + i)
                    .build();
            
            // Assert
            assertEquals(i, record.getBlockNumber());
        }
    }
    
    @Test
    void testBlockchainRecordWithMultipleVotes() {
        // Setup
        User user = User.builder().id(1L).email("voter@example.com").build();
        
        Vote vote1 = Vote.builder().id(1L).title("Vote 1").build();
        Vote vote2 = Vote.builder().id(2L).title("Vote 2").build();
        
        VoteOption option1 = VoteOption.builder().id(1L).optionText("Option 1").build();
        VoteOption option2 = VoteOption.builder().id(2L).optionText("Option 2").build();
        
        BlockchainRecord record1 = BlockchainRecord.builder()
                .blockNumber(1L)
                .currentHash("hash1")
                .user(user)
                .vote(vote1)
                .voteOption(option1)
                .build();
        
        BlockchainRecord record2 = BlockchainRecord.builder()
                .blockNumber(2L)
                .currentHash("hash2")
                .user(user)
                .vote(vote2)
                .voteOption(option2)
                .build();
        
        // Assert - Same user different votes
        assertEquals(user, record1.getUser());
        assertEquals(user, record2.getUser());
        assertNotEquals(record1.getVote().getId(), record2.getVote().getId());
    }
}
