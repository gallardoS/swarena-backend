package dev.swami.arena.leaderboard;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArenaLeaderboardRepositoryTests {

    private JdbcTemplate jdbcTemplate;
    private ArenaLeaderboardRepository repository;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:leaderboard-repository;MODE=MySQL;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute("CREATE SCHEMA acore_characters");
        jdbcTemplate.execute("""
                CREATE TABLE acore_characters.arena_team (
                    arenaTeamId INT PRIMARY KEY,
                    name VARCHAR(24) NOT NULL,
                    type TINYINT NOT NULL,
                    rating SMALLINT NOT NULL,
                    seasonGames SMALLINT NOT NULL,
                    seasonWins SMALLINT NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE acore_characters.arena_team_member (
                    arenaTeamId INT NOT NULL,
                    guid INT NOT NULL,
                    personalRating SMALLINT NOT NULL,
                    PRIMARY KEY (arenaTeamId, guid)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE acore_characters.characters (
                    guid INT PRIMARY KEY,
                    name VARCHAR(12) NOT NULL,
                    class TINYINT NOT NULL
                )
                """);
        repository = new ArenaLeaderboardRepository(jdbcTemplate);
    }

    @Test
    void returnsOnlyPlayedTeamsFromRequestedBracketInRankingOrder() {
        insertTeam(1, "Second", 2, 1700, 20, 12);
        insertTeam(2, "First", 2, 1800, 30, 20);
        insertTeam(3, "Unplayed", 2, 2200, 0, 0);
        insertTeam(4, "Three versus three", 3, 2400, 10, 8);

        List<ArenaLeaderboardRepository.ArenaTeam> teams =
                repository.findTopTeams(ArenaBracket.TWO_V_TWO, 20);

        assertThat(teams).extracting(ArenaLeaderboardRepository.ArenaTeam::name)
                .containsExactly("First", "Second");
    }

    @Test
    void returnsMembersOrderedByPersonalRating() {
        insertTeam(1, "Champions", 2, 1800, 30, 20);
        insertCharacter(10, "Lower", 1);
        insertCharacter(11, "Higher", 8);
        insertMember(1, 10, 1700);
        insertMember(1, 11, 1810);

        ArenaLeaderboardRepository.ArenaTeam team =
                repository.findTopTeams(ArenaBracket.TWO_V_TWO, 20).getFirst();

        assertThat(team.members()).extracting(ArenaLeaderboardRepository.ArenaTeamMember::name)
                .containsExactly("Higher", "Lower");
        assertThat(team.members().getFirst().classId()).isEqualTo(8);
    }

    private void insertTeam(int id, String name, int type, int rating, int games, int wins) {
        jdbcTemplate.update(
                "INSERT INTO acore_characters.arena_team VALUES (?, ?, ?, ?, ?, ?)",
                id, name, type, rating, games, wins
        );
    }

    private void insertCharacter(int guid, String name, int classId) {
        jdbcTemplate.update(
                "INSERT INTO acore_characters.characters VALUES (?, ?, ?)",
                guid, name, classId
        );
    }

    private void insertMember(int teamId, int guid, int rating) {
        jdbcTemplate.update(
                "INSERT INTO acore_characters.arena_team_member VALUES (?, ?, ?)",
                teamId, guid, rating
        );
    }
}
