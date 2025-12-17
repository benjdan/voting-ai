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
            Long userId = extractUserId(httpRequest);
            
            Vote vote = createVoteUseCase.execute(
                    userId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getOptions(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.isUseAIEnhancement()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Vote created successfully", mapToResponse(vote)));
        } catch (Exception e) {
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
            
            BlockchainRecord record = castVoteUseCase.execute(userId, id, request.getVoteOptionId());
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Vote cast successfully. Block #" + record.getBlockNumber(),
                    record.getCurrentHash()
            ));
        } catch (Exception e) {
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
