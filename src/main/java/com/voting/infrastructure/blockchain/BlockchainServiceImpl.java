/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.infrastructure.blockchain;

import com.voting.application.service.BlockchainService;
import com.voting.domain.model.BlockchainRecord;
import com.voting.domain.model.User;
import com.voting.domain.model.Vote;
import com.voting.domain.model.VoteOption;
import com.voting.domain.port.BlockchainRepository;
import com.voting.domain.valueobject.VoteRecord;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockchainServiceImpl implements BlockchainService {
    
	public BlockchainServiceImpl(BlockchainRepository blockchainRepository) {
		this.blockchainRepository = blockchainRepository;
	}
	
	@Autowired
    private BlockchainRepository blockchainRepository;
    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";
    
    @Override
    public BlockchainRecord createVoteBlock(User user, Vote vote, VoteOption voteOption) {
        BlockchainRecord latestBlock = blockchainRepository.findLatestBlock().orElse(null);
        
        long blockNumber = latestBlock == null ? 0 : latestBlock.getBlockNumber() + 1;
        String previousHash = latestBlock == null ? GENESIS_HASH : latestBlock.getCurrentHash();
        
        String data = String.format("User:%d voted for option:%d in vote:%d", 
                user.getId(), voteOption.getId(), vote.getId());
        
        String nonce = String.valueOf(System.currentTimeMillis());
        String currentHash = calculateHash(blockNumber, previousHash, data, nonce);
        
        return BlockchainRecord.builder()
                .blockNumber(blockNumber)
                .previousHash(previousHash)
                .currentHash(currentHash)
                .timestamp(LocalDateTime.now())
                .user(user)
                .vote(vote)
                .voteOption(voteOption)
                .data(data)
                .nonce(nonce)
                .build();
    }
    
    @Override
    public boolean verifyBlockchain() {
        List<BlockchainRecord> blocks = blockchainRepository.findAll();
        
        if (blocks.isEmpty()) {
            return true;
        }
        
        for (int i = 0; i < blocks.size(); i++) {
            BlockchainRecord currentBlock = blocks.get(i);
            
            String calculatedHash = calculateHash(
                    currentBlock.getBlockNumber(),
                    currentBlock.getPreviousHash(),
                    currentBlock.getData(),
                    currentBlock.getNonce()
            );
            
            if (!calculatedHash.equals(currentBlock.getCurrentHash())) {
                return false;
            }
            
            if (i > 0) {
                BlockchainRecord previousBlock = blocks.get(i - 1);
                if (!currentBlock.getPreviousHash().equals(previousBlock.getCurrentHash())) {
                    return false;
                }
            } else {
                if (!currentBlock.getPreviousHash().equals(GENESIS_HASH)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
//    @Override
//    public List<VoteRecord> getVoteHistory(Long voteId) {
//        return blockchainRepository.findByVoteId(voteId).stream()
//                .map(block -> VoteRecord.builder()
//                        .userId(block.getUser().getId())
//                        .userEmail(block.getUser().getEmail())
//                        .voteId(block.getVote().getId())
//                        .voteTitle(block.getVote().getTitle())
//                        .voteOptionId(block.getVoteOption().getId())
//                        .optionText(block.getVoteOption().getOptionText())
//                        .timestamp(block.getTimestamp())
//                        .blockHash(block.getCurrentHash())
//                        .blockNumber(block.getBlockNumber())
//                        .build())
//                .collect(Collectors.toList());

    public List<VoteRecord> getVoteHistory(Long voteId) {
        return blockchainRepository.findByVoteId(voteId)
                .stream()
                .map(this::mapToVoteRecord)
                .collect(Collectors.toList());
    }

    private VoteRecord mapToVoteRecord(BlockchainRecord block) {
        return VoteRecord.builder()
                .userId(block.getUser().getId())
                .userEmail(block.getUser().getEmail())
                .voteId(block.getVote().getId())
                .voteTitle(block.getVote().getTitle())
                .voteOptionId(block.getVoteOption().getId())
                .optionText(block.getVoteOption().getOptionText())
                .timestamp(block.getTimestamp())
                .blockHash(block.getCurrentHash())
                .blockNumber(block.getBlockNumber())
                .build();
    }
    
    
//    public BlockchainRecord mapToDTO(BlockchainRecord record) {
//        return BlockchainRecord.builder()
//            .id(record.getId())
//            .hash(record.getPreviousHash())
//            .previousHash(record.getPreviousHash())
//            .timestamp(record.getTimestamp())
//            .build();
//    }
    
    @Override
    public String calculateHash(Long blockNumber, String previousHash, String data, String nonce) {
        try {
            String input = blockNumber + previousHash + data + nonce;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating hash", e);
        }
    }
}
