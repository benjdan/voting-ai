/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.infrastructure.ai;

import com.voting.domain.model.Vote;
import com.voting.domain.port.VoteRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("xAIService")
@RequiredArgsConstructor
public class XAIServiceImpl {
    
	@Autowired
    private VoteRepository voteRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${xai.api.key:#{null}}")
    private String xaiApiKey;
    
//    @Value("${xai.api.url:https://api.x.ai/v1/chat/completions}")
    @Value("${xai.api.url:#{null}}")
    private String xaiApiUrl;
    
    public String enhanceVoteDescription(String title, String description) {
        if (xaiApiKey == null || xaiApiKey.isEmpty()) {
            return description;
        }
        
        try {
            String prompt = String.format(
                    "Enhance this voting poll description to be more engaging and clear:\n\nTitle: %s\nDescription: %s\n\nProvide an enhanced description.",
                    title, description
            );
            
            return callXAIApi(prompt);
        } catch (Exception e) {
            return description;
        }
    }
    
    public String analyzeVoteResults(Vote vote) {
        if (xaiApiKey == null || xaiApiKey.isEmpty()) {
            return "AI analysis unavailable - xAI API key not configured";
        }
        
        try {
            StringBuilder optionsData = new StringBuilder();
            vote.getOptions().forEach(option -> 
                    optionsData.append(String.format("- %s: %d votes\n", 
                            option.getOptionText(), option.getVoteCount()))
            );
            
            String prompt = String.format(
                    "Analyze the following voting results:\n\nVote: %s\n\nResults:\n%s\nProvide insights.",
                    vote.getTitle(), optionsData
            );
            
            return callXAIApi(prompt);
        } catch (Exception e) {
            return "Error generating analysis: " + e.getMessage();
        }
    }
    
    public String generateVoteInsights(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found"));
        
        return analyzeVoteResults(vote);
    }
    
    private String callXAIApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(xaiApiKey);
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", List.of(message));
        requestBody.put("model", "grok-beta");
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 300);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
                xaiApiUrl,
                HttpMethod.POST,
                request,
                Map.class
        );
        
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message1 = (Map<String, Object>) choices.get(0).get("message");
                return (String) message1.get("content");
            }
        }
        
        throw new RuntimeException("Invalid response from xAI API");
    }
}
