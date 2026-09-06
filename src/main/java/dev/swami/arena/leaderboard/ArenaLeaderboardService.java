package dev.swami.arena.leaderboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongSupplier;
import java.util.stream.IntStream;

@Service
class ArenaLeaderboardService {

    static final int DEFAULT_LIMIT = 20;
    static final int MAX_LIMIT = 100;

    private final ArenaLeaderboardRepository repository;
    private final LeaderboardProperties properties;
    private final LongSupplier nanoTime;
    private final ConcurrentMap<CacheKey, CachedLeaderboard> cache = new ConcurrentHashMap<>();

    @Autowired
    ArenaLeaderboardService(ArenaLeaderboardRepository repository, LeaderboardProperties properties) {
        this(repository, properties, System::nanoTime);
    }

    ArenaLeaderboardService(
            ArenaLeaderboardRepository repository,
            LeaderboardProperties properties,
            LongSupplier nanoTime
    ) {
        this.repository = repository;
        this.properties = properties;
        this.nanoTime = nanoTime;
    }

    ArenaLeaderboardResponse getLeaderboard(String bracketValue, int limit) {
        ArenaBracket bracket = ArenaBracket.from(bracketValue);
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new InvalidLeaderboardRequestException("Limit must be between 1 and " + MAX_LIMIT);
        }

        CacheKey key = new CacheKey(bracket, limit);
        long now = nanoTime.getAsLong();
        CachedLeaderboard cached = cache.get(key);
        if (cached != null && now - cached.createdAtNanos() < properties.cacheTtl().toNanos()) {
            return cached.response();
        }

        ArenaLeaderboardResponse response = mapResponse(bracket, repository.findTopTeams(bracket, limit));
        cache.put(key, new CachedLeaderboard(response, now));
        return response;
    }

    private static ArenaLeaderboardResponse mapResponse(
            ArenaBracket bracket,
            List<ArenaLeaderboardRepository.ArenaTeam> teams
    ) {
        List<ArenaTeamResponse> responseTeams = IntStream.range(0, teams.size())
                .mapToObj(index -> {
                    ArenaLeaderboardRepository.ArenaTeam team = teams.get(index);
                    int losses = Math.max(0, team.seasonGames() - team.seasonWins());
                    double winRate = team.seasonGames() == 0
                            ? 0.0
                            : Math.round(team.seasonWins() * 1000.0 / team.seasonGames()) / 10.0;
                    List<ArenaTeamMemberResponse> members = team.members().stream()
                            .map(member -> new ArenaTeamMemberResponse(
                                    member.name(),
                                    member.classId(),
                                    member.personalRating()
                            ))
                            .toList();
                    return new ArenaTeamResponse(
                            index + 1,
                            team.name(),
                            team.rating(),
                            team.seasonGames(),
                            team.seasonWins(),
                            losses,
                            winRate,
                            members
                    );
                })
                .toList();
        return new ArenaLeaderboardResponse(bracket.value(), responseTeams);
    }

    private record CacheKey(ArenaBracket bracket, int limit) {
    }

    private record CachedLeaderboard(ArenaLeaderboardResponse response, long createdAtNanos) {
    }
}
