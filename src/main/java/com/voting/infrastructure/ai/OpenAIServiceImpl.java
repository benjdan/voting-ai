/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.infrastructure.ai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.voting.application.service.AIService;
import com.voting.domain.model.Vote;
import com.voting.domain.port.VoteRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAIServiceImpl implements AIService {
    
	@Autowired
    private VoteRepository voteRepository;
    
    @Value("${openai.api.key}")
    private String openaiApiKey;
    
    @Override
    public String enhanceVoteDescription(String title, String description) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return description;
        }
        
        try {
            OpenAiService service = new OpenAiService(openaiApiKey, Duration.ofSeconds(30));
            
            String prompt = String.format(
                    "Enhance this voting poll description to be more engaging and clear:\n\nTitle: %s\nDescription: %s\n\nProvide an enhanced description that is clear, concise, and engaging.",
                    title, description
            );
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(List.of(new ChatMessage("user", prompt)))
                    .maxTokens(200)
                    .temperature(0.7)
                    .build();
            
            String result = service.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
            
            service.shutdownExecutor();
            return result;
        } catch (Exception e) {
            return description;
        }
    }
    
    @Override
    public String analyzeVoteResults(Vote vote) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return "AI analysis unavailable - API key not configured";
        }
        
        try {
            OpenAiService service = new OpenAiService(openaiApiKey, Duration.ofSeconds(30));
            
            StringBuilder optionsData = new StringBuilder();
            vote.getOptions().forEach(option -> 
                    optionsData.append(String.format("- %s: %d votes\n", 
                            option.getOptionText(), option.getVoteCount()))
            );
            
            String prompt = String.format(
                    "Analyze the following voting results:\n\nVote: %s\n\nResults:\n%s\nProvide insights about the voting patterns and what they might indicate.",
                    vote.getTitle(), optionsData
            );
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(List.of(new ChatMessage("user", prompt)))
                    .maxTokens(300)
                    .temperature(0.7)
                    .build();
            
            String result = service.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
            
            service.shutdownExecutor();
            return result;
        } catch (Exception e) {
            return "Error generating analysis: " + e.getMessage();
        }
    }
    
    @Override
    public String generateVoteInsights(Long voteId) {
        Vote vote;
		try {
			vote = voteRepository.findByCreatorId(voteId).get(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException("Vote not found="+e.getLocalizedMessage());
		}
//                new IllegalArgumentException("Vote not found"));
        
        return analyzeVoteResults(vote);
    }
}
