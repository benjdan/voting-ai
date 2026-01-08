package com.voting.infrastructure.blockchain;

import com.voting.domain.model.BlockchainRecord;
import com.voting.domain.model.User;
import com.voting.domain.model.Vote;
import com.voting.domain.model.VoteOption;
import com.voting.domain.port.BlockchainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceImplTest {
    
    @Mock
    private BlockchainRepository blockchainRepository;
    
    private BlockchainServiceImpl blockchainService;
    
    @BeforeEach
    void setUp() {
        blockchainService = new BlockchainServiceImpl(blockchainRepository);
    }
    
    @Test
    void testCreateVoteBlock_FirstBlock() {
        User user = User.builder().id(1L).email("test@example.com").build();
        Vote vote = Vote.builder().id(1L).title("Test Vote").build();
        VoteOption option = VoteOption.builder().id(1L).optionText("Option 1").build();
        
        when(blockchainRepository.findLatestBlock()).thenReturn(Optional.empty());
        
        BlockchainRecord record = blockchainService.createVoteBlock(user, vote, option);
        
        assertNotNull(record);
        assertEquals(0L, record.getBlockNumber());
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000", record.getPreviousHash());
        assertNotNull(record.getCurrentHash());
        assertEquals(user, record.getUser());
        assertEquals(vote, record.getVote());
        assertEquals(option, record.getVoteOption());
    }
    
    @Test
    void testCreateVoteBlock_SubsequentBlock() {
        User user = User.builder().id(1L).email("test@example.com").build();
        Vote vote = Vote.builder().id(1L).title("Test Vote").build();
        VoteOption option = VoteOption.builder().id(1L).optionText("Option 1").build();
        
        BlockchainRecord previousBlock = BlockchainRecord.builder()
                .blockNumber(0L)
                .currentHash("previoushash123")
                .build();
        
        when(blockchainRepository.findLatestBlock()).thenReturn(Optional.of(previousBlock));
        
        BlockchainRecord record = blockchainService.createVoteBlock(user, vote, option);
        
        assertNotNull(record);
        assertEquals(1L, record.getBlockNumber());
        assertEquals("previoushash123", record.getPreviousHash());
        assertNotNull(record.getCurrentHash());
    }
    
    @Test
    void testCalculateHash_ConsistentResults() {
        String hash1 = blockchainService.calculateHash(0L, "prev", "data", "nonce");
        String hash2 = blockchainService.calculateHash(0L, "prev", "data", "nonce");
        
        assertEquals(hash1, hash2);
    }
    
    @Test
    void testCalculateHash_DifferentInputsDifferentHashes() {
        String hash1 = blockchainService.calculateHash(0L, "prev", "data1", "nonce");
        String hash2 = blockchainService.calculateHash(0L, "prev", "data2", "nonce");
        
        assertNotEquals(hash1, hash2);
    }
    
    @Test
    void testVerifyBlockchain_ValidChain() {
        User user = User.builder().id(1L).build();
        Vote vote = Vote.builder().id(1L).build();
        VoteOption option = VoteOption.builder().id(1L).build();
        
        String genesisHash = "0000000000000000000000000000000000000000000000000000000000000000";
        String hash1 = blockchainService.calculateHash(0L, genesisHash, "data1", "nonce1");
        String hash2 = blockchainService.calculateHash(1L, hash1, "data2", "nonce2");
        
        BlockchainRecord block1 = BlockchainRecord.builder()
                .blockNumber(0L)
                .previousHash(genesisHash)
                .currentHash(hash1)
                .data("data1")
                .nonce("nonce1")
                .user(user)
                .vote(vote)
                .voteOption(option)
                .build();
        
        BlockchainRecord block2 = BlockchainRecord.builder()
                .blockNumber(1L)
                .previousHash(hash1)
                .currentHash(hash2)
                .data("data2")
                .nonce("nonce2")
                .user(user)
                .vote(vote)
                .voteOption(option)
                .build();
        
        when(blockchainRepository.findAll()).thenReturn(Arrays.asList(block1, block2));
        
        assertTrue(blockchainService.verifyBlockchain());
    }
    
    @Test
    void testVerifyBlockchain_EmptyChain() {
        when(blockchainRepository.findAll()).thenReturn(List.of());
        
        assertTrue(blockchainService.verifyBlockchain());
    }
}
