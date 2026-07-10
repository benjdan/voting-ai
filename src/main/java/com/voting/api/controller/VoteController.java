/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.controller;

import com.voting.api.dto.*;
import com.voting.application.usecase.CastVoteUseCase;
import com.voting.application.usecase.CreateVoteUseCase;
import com.voting.application.usecase.GetVoteHistoryUseCase;
import com.voting.domain.model.BlockchainRecord;
import com.voting.domain.model.Vote;
import com.voting.domain.model.VoteOption;
import com.voting.domain.port.VoteRepository;
import com.voting.domain.valueobject.VoteRecord;
import com.voting.infrastructure.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/votes")
@AllArgsConstructor
@CrossOrigin(origins = "*")
@Builder
public class VoteController {
    private static final Logger LOGGER = LogManager.getLogger(VoteController.class);

	@Autowired
    private CreateVoteUseCase createVoteUseCase;
	@Autowired
    private CastVoteUseCase castVoteUseCase;
	@Autowired
    private GetVoteHistoryUseCase getVoteHistoryUseCase;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping
    public ResponseEntity<ApiResponse<VoteResponse>> createVote(
            @Valid @RequestBody CreateVoteRequest request,
            HttpServletRequest httpRequest) {
        try {
            LOGGER.info("Received request to create vote with title={}", request.getTitle());
            Long userId = extractUserId(httpRequest);
            LOGGER.debug("Creating vote for userId={}, title={}", userId, request.getTitle());
            
            Vote vote = createVoteUseCase.execute(
                    userId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getOptions(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.isUseAIEnhancement()
            );
            
            LOGGER.info("Vote created successfully with voteId={}, title={}, creator={}", vote.getId(), vote.getTitle(), userId);
            return ResponseEntity.ok(ApiResponse.success("Vote created successfully", mapToResponse(vote)));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid argument while creating vote: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Unexpected error while creating vote with title={}", request.getTitle(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<VoteResponse>>> getAllVotes() {
        List<VoteResponse> votes = voteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(votes));
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<VoteResponse>>> getActiveVotes() {
        List<VoteResponse> votes = voteRepository.findActiveVotes().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(votes));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoteResponse>> getVote(@PathVariable Long id) {
        return voteRepository.findById(id)
                .map(vote -> ResponseEntity.ok(ApiResponse.success(mapToResponse(vote))))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/cast")
    public ResponseEntity<ApiResponse<String>> castVote(
            @PathVariable Long id,
            @Valid @RequestBody CastVoteRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            LOGGER.debug("User userId={} casting vote for voteId={}, optionId={}", userId, id, request.getVoteOptionId());
            
            BlockchainRecord record = castVoteUseCase.execute(userId, id, request.getVoteOptionId());
            
            LOGGER.info("Vote cast successfully for voteId={}, userId={}, blockNumber={}", id, userId, record.getBlockNumber());
            return ResponseEntity.ok(ApiResponse.success(
                    "Vote cast successfully. Block #" + record.getBlockNumber(),
                    record.getCurrentHash()
            ));
        } catch (IllegalStateException e) {
            LOGGER.error("Invalid vote state for voteId={}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid argument while casting vote for voteId={}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Unexpected error while casting vote for voteId={}", id, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<VoteRecord>>> getVoteHistory(@PathVariable Long id) {
        List<VoteRecord> history = getVoteHistoryUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
    
    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("User not authenticated");
    }
    
    private VoteResponse mapToResponse(Vote vote) {
    	List<VoteOptionResponse> v = new ArrayList<VoteOptionResponse>();
        return VoteResponse.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .description(vote.getDescription())
                .aiEnhancedDescription(vote.getAiEnhancedDescription())
                .creatorName(vote.getCreator().getName())
                .startDate(vote.getStartDate())
                .endDate(vote.getEndDate())
                .active(vote.getActive())
                .open(vote.isOpen())
                .createdAt(vote.getCreatedAt())
                .options(vote.getOptions().stream()
                        .map(this::mapToVoteRecord)
                        .collect(Collectors.toList()))
                .build();
    }
    
    private VoteOptionResponse mapToVoteRecord(VoteOption option) {
        return VoteOptionResponse.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .voteCount(option.getVoteCount())
                .build();
    }
    
//    private List<VoteOptionResponse> mapToVoteRecord(VoteOption option) {
//    	List<VoteOptionResponse> v = new ArrayList<VoteOptionResponse>();
//    	v.add(VoteOptionResponse.builder()
//                .id(option.getId())
//                .optionText(option.getOptionText())
//                .voteCount(option.getVoteCount())
//                .build());
//    			
//    }
}
