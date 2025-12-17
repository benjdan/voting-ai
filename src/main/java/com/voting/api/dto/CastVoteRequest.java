/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CastVoteRequest {
    
    @NotNull(message = "Vote option ID is required")
    private Long voteOptionId;

	public Long getVoteOptionId() {
		return voteOptionId;
	}

	public void setVoteOptionId(Long voteOptionId) {
		this.voteOptionId = voteOptionId;
	}
}
