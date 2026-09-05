package dev.swami.arena.server;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServerStatusControllerTests {

    @Test
    void exposesTheOnlineStateExpectedByTheFrontend() throws Exception {
        GameServerStatusService statusService = mock(GameServerStatusService.class);
        when(statusService.isOnline()).thenReturn(true);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ServerStatusController(statusService))
                .build();

        mockMvc.perform(get("/api/v1/server/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.online").value(true));
    }
}
