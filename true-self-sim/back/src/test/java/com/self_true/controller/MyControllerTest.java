package com.self_true.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.self_true.model.dto.request.PrivateSceneRequest;
import com.self_true.service.PrivateStoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyController.class)
class MyControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    PrivateStoryService privateStoryService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createOrUpdate() throws Exception {
        PrivateSceneRequest req = new PrivateSceneRequest();
        req.setSceneId("a");
        req.setSpeaker("sp");
        req.setBackgroundImage("bg");
        req.setText("t");
        req.setChoiceRequests(List.of());
        mvc.perform(post("/my/scene/a")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
