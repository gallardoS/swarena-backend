package dev.swami.arena.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/server")
class ServerStatusController {

    private final GameServerStatusService statusService;

    ServerStatusController(GameServerStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    ServerStatusResponse status() {
        return new ServerStatusResponse(statusService.isOnline());
    }
}
