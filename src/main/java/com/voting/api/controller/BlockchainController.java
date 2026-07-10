/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.controller;

import com.voting.api.dto.ApiResponse;
import com.voting.application.service.BlockchainService;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BlockchainController {
    private static final Logger LOGGER = LogManager.getLogger(BlockchainController.class);
	@Autowired
    private BlockchainService blockchainService;
    
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyBlockchain() {
		LOGGER.info("Received request to verify blockchain integrity");
        try {
            boolean isValid = blockchainService.verifyBlockchain();
            String message = isValid ? "Blockchain is valid and tamper-proof" : "Blockchain integrity compromised";
            
            if (isValid) {
                LOGGER.info("Blockchain verification successful - blockchain is valid and tamper-proof");
            } else {
                LOGGER.warn("Blockchain verification failed - integrity compromised detected");
            }
            
            return ResponseEntity.ok(ApiResponse.success(message, isValid));
        } catch (Exception e) {
            LOGGER.error("Error during blockchain verification", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Blockchain verification failed: " + e.getMessage()));
        }
    }
}
