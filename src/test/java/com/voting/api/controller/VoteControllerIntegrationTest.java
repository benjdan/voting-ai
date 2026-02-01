package com.voting.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voting.api.dto.CastVoteRequest;
import com.voting.api.dto.CreateVoteRequest;
import com.voting.api.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VoteControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String authToken;
    
    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("votetest" + System.currentTimeMillis() + "@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Vote Test User");
        
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response).get("data").get("token").asText();
    }
    
    @Test
    void testCreateVote_Success() throws Exception {
        CreateVoteRequest request = new CreateVoteRequest();
        request.setTitle("Favorite Color");
        request.setDescription("Vote for your favorite color");
        request.setOptions(Arrays.asList("Red", "Blue", "Green"));
        request.setStartDate(LocalDateTime.now().minusHours(1));
        request.setEndDate(LocalDateTime.now().plusDays(7));
        
        mockMvc.perform(post("/api/votes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Favorite Color"))
                .andExpect(jsonPath("$.data.options").isArray())
                .andExpect(jsonPath("$.data.options.length()").value(3));
    }
    
    @Test
    void testGetAllVotes() throws Exception {
        mockMvc.perform(get("/api/votes")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testGetActiveVotes() throws Exception {
        mockMvc.perform(get("/api/votes/active")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testCastVote_Success() throws Exception {
        CreateVoteRequest createRequest = new CreateVoteRequest();
        createRequest.setTitle("Test Poll");
        createRequest.setDescription("A test poll");
        createRequest.setOptions(Arrays.asList("Option A", "Option B"));
        createRequest.setStartDate(LocalDateTime.now().minusHours(1));
        createRequest.setEndDate(LocalDateTime.now().plusDays(1));
        
        MvcResult createResult = mockMvc.perform(post("/api/votes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();
        
        String createResponse = createResult.getResponse().getContentAsString();
        Long voteId = objectMapper.readTree(createResponse).get("data").get("id").asLong();
        Long optionId = objectMapper.readTree(createResponse).get("data").get("options").get(0).get("id").asLong();
        
        CastVoteRequest castRequest = new CastVoteRequest();
        castRequest.setVoteOptionId(optionId);
        
        mockMvc.perform(post("/api/votes/" + voteId + "/cast")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(castRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    void testGetVoteHistory() throws Exception {
        CreateVoteRequest createRequest = new CreateVoteRequest();
        createRequest.setTitle("History Test");
        createRequest.setDescription("Test vote history");
        createRequest.setOptions(Arrays.asList("Yes", "No"));
        createRequest.setStartDate(LocalDateTime.now().minusHours(1));
        createRequest.setEndDate(LocalDateTime.now().plusDays(1));
        
        MvcResult createResult = mockMvc.perform(post("/api/votes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();
        
        String createResponse = createResult.getResponse().getContentAsString();
        Long voteId = objectMapper.readTree(createResponse).get("data").get("id").asLong();
        
        mockMvc.perform(get("/api/votes/" + voteId + "/history")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
