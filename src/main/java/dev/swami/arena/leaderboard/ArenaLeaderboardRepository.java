package dev.swami.arena.leaderboard;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
class ArenaLeaderboardRepository {

    private static final String FIND_TOP_TEAMS = """
            SELECT arenaTeamId, name, rating, seasonGames, seasonWins
            FROM acore_characters.arena_team
            WHERE type = ? AND seasonGames > 0
            ORDER BY rating DESC, seasonWins DESC, seasonGames ASC, arenaTeamId ASC
            LIMIT ?
            """;

    private static final String FIND_MEMBERS = """
            SELECT atm.arenaTeamId, c.name, c.class, atm.personalRating
            FROM acore_characters.arena_team_member atm
            INNER JOIN acore_characters.characters c ON c.guid = atm.guid
            WHERE atm.arenaTeamId IN (:teamIds)
            ORDER BY atm.arenaTeamId, atm.personalRating DESC, c.name ASC
            """;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    ArenaLeaderboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    List<ArenaTeam> findTopTeams(ArenaBracket bracket, int limit) {
        List<TeamRow> teams = jdbcTemplate.query(
                FIND_TOP_TEAMS,
                (resultSet, rowNumber) -> new TeamRow(
                        resultSet.getLong("arenaTeamId"),
                        resultSet.getString("name"),
                        resultSet.getInt("rating"),
                        resultSet.getInt("seasonGames"),
                        resultSet.getInt("seasonWins")
                ),
                bracket.databaseType(),
                limit
        );

        if (teams.isEmpty()) {
            return List.of();
        }

        Map<Long, List<ArenaTeamMember>> membersByTeam = new LinkedHashMap<>();
        List<Long> teamIds = teams.stream().map(TeamRow::id).toList();
        namedJdbcTemplate.query(
                FIND_MEMBERS,
                new MapSqlParameterSource("teamIds", teamIds),
                (RowCallbackHandler) resultSet -> membersByTeam
                        .computeIfAbsent(resultSet.getLong("arenaTeamId"), ignored -> new ArrayList<>())
                        .add(new ArenaTeamMember(
                                resultSet.getString("name"),
                                resultSet.getInt("class"),
                                resultSet.getInt("personalRating")
                        ))
        );

        return teams.stream()
                .map(team -> new ArenaTeam(
                        team.name(),
                        team.rating(),
                        team.seasonGames(),
                        team.seasonWins(),
                        List.copyOf(membersByTeam.getOrDefault(team.id(), List.of()))
                ))
                .toList();
    }

    record ArenaTeam(String name, int rating, int seasonGames, int seasonWins, List<ArenaTeamMember> members) {
    }

    record ArenaTeamMember(String name, int classId, int personalRating) {
    }

    private record TeamRow(long id, String name, int rating, int seasonGames, int seasonWins) {
    }
}
