/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.controller;

import com.voting.api.dto.ApiResponse;
import com.voting.application.service.BlockchainService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BlockchainController {
    
	@Autowired
    private BlockchainService blockchainService;
    
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyBlockchain() {
        boolean isValid = blockchainService.verifyBlockchain();
        String message = isValid ? "Blockchain is valid and tamper-proof" : "Blockchain integrity compromised";
        return ResponseEntity.ok(ApiResponse.success(message, isValid));
    }
}
