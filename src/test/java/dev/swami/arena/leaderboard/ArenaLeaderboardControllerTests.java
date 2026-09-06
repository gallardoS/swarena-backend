package dev.swami.arena.leaderboard;

import dev.swami.arena.api.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArenaLeaderboardControllerTests {

    @Test
    void exposesLeaderboardContractAndCacheHeader() throws Exception {
        ArenaLeaderboardService service = mock(ArenaLeaderboardService.class);
        LeaderboardProperties properties = new LeaderboardProperties(Duration.ofSeconds(30));
        when(service.getLeaderboard("2v2", 20)).thenReturn(new ArenaLeaderboardResponse(
                "2v2",
                List.of(new ArenaTeamResponse(
                        1,
                        "Champions",
                        1900,
                        12,
                        8,
                        4,
                        66.7,
                        List.of(new ArenaTeamMemberResponse("Mage", 8, 1880))
                ))
        ));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ArenaLeaderboardController(service, properties))
                .build();

        mockMvc.perform(get("/api/v1/leaderboards/arenas").param("bracket", "2v2"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=30, public"))
                .andExpect(jsonPath("$.bracket").value("2v2"))
                .andExpect(jsonPath("$.teams[0].position").value(1))
                .andExpect(jsonPath("$.teams[0].members[0].classId").value(8));
    }

    @Test
    void returnsProblemDetailForInvalidBracket() throws Exception {
        ArenaLeaderboardService service = mock(ArenaLeaderboardService.class);
        LeaderboardProperties properties = new LeaderboardProperties(Duration.ofSeconds(30));
        when(service.getLeaderboard("1v1", 20))
                .thenThrow(new InvalidLeaderboardRequestException("Bracket must be one of: 2v2, 3v3, 5v5"));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ArenaLeaderboardController(service, properties))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/leaderboards/arenas").param("bracket", "1v1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_LEADERBOARD_REQUEST"));
    }
}
