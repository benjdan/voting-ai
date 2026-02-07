/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.application.usecase;

import com.voting.application.service.AIService;
import com.voting.domain.model.User;
import com.voting.domain.model.Vote;
import com.voting.domain.model.VoteOption;
import com.voting.domain.port.UserRepository;
import com.voting.domain.port.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateVoteUseCaseTest {
    
    @Mock
    private VoteRepository voteRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AIService aiService;
    
    private CreateVoteUseCase createVoteUseCase;
    
    @BeforeEach
    void setUp() {
        createVoteUseCase = new CreateVoteUseCase(voteRepository, userRepository, aiService);
    }
    
    @Test
    void testExecute_SuccessfulVoteCreation() {
        Long creatorId = 1L;
        User creator = User.builder()
                .id(creatorId)
                .email("creator@example.com")
                .name("Vote Creator")
                .active(true)
                .build();
        
        String title = "Implement feature X?";
        String description = "To improve user experience";
        List<String> options = List.of("Yes", "No", "Abstain");
        LocalDateTime startDate = LocalDateTime.now().plusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        Vote expectedVote = Vote.builder()
                .id(1L)
                .title(title)
                .description(description)
                .creator(creator)
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .build();
        
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(voteRepository.save(any(Vote.class))).thenReturn(expectedVote);
        
        Vote result = createVoteUseCase.execute(creatorId, title, description, options, startDate, endDate, false);
        
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(creator, result.getCreator());
        assertTrue(result.getActive());
        verify(userRepository).findById(creatorId);
        verify(voteRepository).save(any(Vote.class));
    }
    
    @Test
    void testExecute_VoteCreationWithAIEnhancement() {
        Long creatorId = 1L;
        User creator = User.builder()
                .id(creatorId)
                .email("creator@example.com")
                .name("Vote Creator")
                .build();
        
        String title = "Best programming language?";
        String description = "Which language is most productive";
        String aiEnhanced = "Which programming language?";
        List<String> options = List.of("Java", "Python", "Go");
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        
        Vote expectedVote = Vote.builder()
                .id(1L)
                .title(title)
                .description(description)
                .aiEnhancedDescription(aiEnhanced)
                .creator(creator)
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .build();
        
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(aiService.enhanceVoteDescription(title, description)).thenReturn(aiEnhanced);
        when(voteRepository.save(any(Vote.class))).thenReturn(expectedVote);
        
        Vote result = createVoteUseCase.execute(creatorId, title, description, options, startDate, endDate, true);
        
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(aiEnhanced, result.getAiEnhancedDescription());
        verify(aiService).enhanceVoteDescription(title, description);
        verify(voteRepository).save(any(Vote.class));
    }
    
    @Test
    void testExecute_AIEnhancementFails_ContinuesCreation() {
        Long creatorId = 1L;
        User creator = User.builder()
                .id(creatorId)
                .email("creator@example.com")
                .name("Vote Creator")
                .build();
        
        String title = "Test Vote";
        String description = "Test description";
        List<String> options = List.of("Option 1", "Option 2");
        
        Vote expectedVote = Vote.builder()
                .id(1L)
                .title(title)
                .description(description)
                .aiEnhancedDescription(null)
                .creator(creator)
                .active(true)
                .build();
        
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(aiService.enhanceVoteDescription(anyString(), anyString()))
                .thenThrow(new RuntimeException("AI Service down"));
        when(voteRepository.save(any(Vote.class))).thenReturn(expectedVote);
        
        Vote result = createVoteUseCase.execute(creatorId, title, description, options, 
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), true);
        
        assertNotNull(result);
        assertNull(result.getAiEnhancedDescription());
        verify(voteRepository).save(any(Vote.class));
    }
    
    @Test
    void testExecute_CreatorNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () ->
                createVoteUseCase.execute(999L, "Title", "Description", 
                        List.of("Option 1"), LocalDateTime.now(), LocalDateTime.now().plusDays(1), false)
        );
        
        verify(userRepository).findById(999L);
        verify(voteRepository, never()).save(any());
    }
    
    @Test
    void testExecute_MultipleOptionsCreated() {
        Long creatorId = 1L;
        User creator = User.builder().id(creatorId).email("test@example.com").name("Test").build();
        
        List<String> options = List.of("Option 1", "Option 2", "Option 3", "Option 4");
        Vote expectedVote = Vote.builder()
                .id(1L)
                .title("Multiple Options Vote")
                .creator(creator)
                .active(true)
                .build();
        
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(voteRepository.save(any(Vote.class))).thenAnswer(invocation -> {
            Vote vote = invocation.getArgument(0);
            for (String optionText : options) {
                VoteOption option = VoteOption.builder()
                        .optionText(optionText)
                        .voteCount(0)
                        .build();
                vote.getOptions().add(option);
            }
            return vote;
        });
        
        Vote result = createVoteUseCase.execute(creatorId, "Multiple Options Vote", "Description", 
                options, LocalDateTime.now(), LocalDateTime.now().plusDays(1), false);
        
        assertNotNull(result);
        assertEquals(4, result.getOptions().size());
    }
    
    @Test
    void testExecute_EmptyDescription_WithoutAIEnhancement() {
        Long creatorId = 1L;
        User creator = User.builder().id(creatorId).email("test@example.com").name("Test").build();
        
        Vote expectedVote = Vote.builder()
                .id(1L)
                .title("Vote without description")
                .description("")
                .creator(creator)
                .active(true)
                .build();
        
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(voteRepository.save(any(Vote.class))).thenReturn(expectedVote);
        
        Vote result = createVoteUseCase.execute(creatorId, "Vote without description", "", 
                List.of("Yes", "No"), LocalDateTime.now(), LocalDateTime.now().plusDays(1), true);
        
        assertNotNull(result);
        assertEquals("", result.getDescription());
        verify(aiService, never()).enhanceVoteDescription(anyString(), anyString());
    }
}
