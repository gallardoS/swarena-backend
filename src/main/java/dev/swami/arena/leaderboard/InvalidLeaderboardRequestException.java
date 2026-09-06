package dev.swami.arena.leaderboard;

public class InvalidLeaderboardRequestException extends RuntimeException {

    InvalidLeaderboardRequestException(String message) {
        super(message);
    }
}
