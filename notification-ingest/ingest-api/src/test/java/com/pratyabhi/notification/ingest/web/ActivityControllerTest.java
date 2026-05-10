package com.pratyabhi.notification.ingest.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "ingest.pulsar-enabled=false",
      "ingest.api-keys=test-key,other-key",
    })
class ActivityControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void unauthorizedWithoutApiKey() throws Exception {
    mockMvc
        .perform(
            post("/v1/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "idem-1")
                .content(
                    """
                    {
                      "recipientId": "u1",
                      "activityType": "MESSAGE_SENT",
                      "sourceService": "messaging"
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void acceptedWithApiKey() throws Exception {
    mockMvc
        .perform(
            post("/v1/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Api-Key", "test-key")
                .header("Idempotency-Key", "idem-2")
                .content(
                    """
                    {
                      "recipientId": "u1",
                      "activityType": "MESSAGE_SENT",
                      "sourceService": "messaging"
                    }
                    """))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.eventId").exists());
  }
}
