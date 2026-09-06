package dev.swami.arena.leaderboard;

import java.util.List;

record ArenaLeaderboardResponse(String bracket, List<ArenaTeamResponse> teams) {
}

record ArenaTeamResponse(
        int position,
        String name,
        int rating,
        int seasonGames,
        int seasonWins,
        int seasonLosses,
        double winRate,
        List<ArenaTeamMemberResponse> members
) {
}

record ArenaTeamMemberResponse(String name, int classId, int personalRating) {
}
