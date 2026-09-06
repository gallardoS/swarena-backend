package dev.swami.arena.leaderboard;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/leaderboards")
class ArenaLeaderboardController {

    private final ArenaLeaderboardService service;
    private final LeaderboardProperties properties;

    ArenaLeaderboardController(ArenaLeaderboardService service, LeaderboardProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @GetMapping("/arenas")
    ResponseEntity<ArenaLeaderboardResponse> arenas(
            @RequestParam String bracket,
            @RequestParam(defaultValue = "20") int limit
    ) {
        ArenaLeaderboardResponse response = service.getLeaderboard(bracket, limit);
        long maxAge = Math.max(0, properties.cacheTtl().toSeconds());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(maxAge, TimeUnit.SECONDS).cachePublic())
                .body(response);
    }
}
