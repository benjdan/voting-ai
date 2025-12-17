/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vote_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Vote getVote() {
		return vote;
	}

	public void setVote(Vote vote) {
		this.vote = vote;
	}

	public String getOptionText() {
		return optionText;
	}

	public void setOptionText(String optionText) {
		this.optionText = optionText;
	}

	public Integer getVoteCount() {
		return voteCount;
	}

	public void setVoteCount(Integer voteCount) {
		this.voteCount = voteCount;
	}

	@ManyToOne
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;
    
    @Column(nullable = false)
    private String optionText;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer voteCount = 0;
    
    public void incrementVoteCount() {
        this.voteCount++;
    }
}
