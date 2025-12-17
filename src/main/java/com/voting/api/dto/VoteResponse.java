/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {
    private Long id;
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAiEnhancedDescription() {
		return aiEnhancedDescription;
	}
	public void setAiEnhancedDescription(String aiEnhancedDescription) {
		this.aiEnhancedDescription = aiEnhancedDescription;
	}
	public String getCreatorName() {
		return creatorName;
	}
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	public LocalDateTime getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}
	public LocalDateTime getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
	public List<VoteOptionResponse> getOptions() {
		return options;
	}
	public void setOptions(List<VoteOptionResponse> options) {
		this.options = options;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	private String title;
    private String description;
    private String aiEnhancedDescription;
    private String creatorName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private boolean open;
    private List<VoteOptionResponse> options;
    private LocalDateTime createdAt;
}
