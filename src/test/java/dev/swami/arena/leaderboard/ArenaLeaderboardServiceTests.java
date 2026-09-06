package dev.swami.arena.leaderboard;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArenaLeaderboardServiceTests {

    @Test
    void mapsStatisticsAndCachesIdenticalRequests() {
        ArenaLeaderboardRepository repository = mock(ArenaLeaderboardRepository.class);
        when(repository.findTopTeams(ArenaBracket.TWO_V_TWO, 20)).thenReturn(List.of(
                new ArenaLeaderboardRepository.ArenaTeam(
                        "Champions",
                        1900,
                        12,
                        8,
                        List.of(new ArenaLeaderboardRepository.ArenaTeamMember("Mage", 8, 1880))
                )
        ));
        AtomicLong time = new AtomicLong();
        ArenaLeaderboardService service = new ArenaLeaderboardService(
                repository,
                new LeaderboardProperties(Duration.ofSeconds(30)),
                time::get
        );

        ArenaLeaderboardResponse first = service.getLeaderboard("2v2", 20);
        ArenaLeaderboardResponse second = service.getLeaderboard("2V2", 20);

        assertThat(second).isSameAs(first);
        assertThat(first.bracket()).isEqualTo("2v2");
        assertThat(first.teams().getFirst().seasonLosses()).isEqualTo(4);
        assertThat(first.teams().getFirst().winRate()).isEqualTo(66.7);
        verify(repository, times(1)).findTopTeams(ArenaBracket.TWO_V_TWO, 20);
    }

    @Test
    void rejectsUnsupportedBracketAndLimit() {
        ArenaLeaderboardRepository repository = mock(ArenaLeaderboardRepository.class);
        ArenaLeaderboardService service = new ArenaLeaderboardService(
                repository,
                new LeaderboardProperties(Duration.ofSeconds(30))
        );

        assertThatThrownBy(() -> service.getLeaderboard("1v1", 20))
                .isInstanceOf(InvalidLeaderboardRequestException.class);
        assertThatThrownBy(() -> service.getLeaderboard("2v2", 101))
                .isInstanceOf(InvalidLeaderboardRequestException.class);
    }
}
