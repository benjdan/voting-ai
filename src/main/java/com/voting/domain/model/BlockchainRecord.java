/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "blockchain_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockchainRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(nullable = false)
    private Long blockNumber;
    
    @Column(nullable = false)
    private String previousHash;
    
    @Column(nullable = false)
    private String currentHash;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;
    
    @ManyToOne
    @JoinColumn(name = "vote_option_id", nullable = false)
    private VoteOption voteOption;
    
    @Column(nullable = false, length = 1000)
    private String data;
    
    @Column(nullable = false)
    private String nonce;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
