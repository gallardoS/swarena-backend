package dev.swami.arena.leaderboard;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("swarena.leaderboard")
record LeaderboardProperties(Duration cacheTtl) {
}
