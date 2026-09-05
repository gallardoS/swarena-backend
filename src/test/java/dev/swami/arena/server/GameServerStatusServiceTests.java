package dev.swami.arena.server;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class GameServerStatusServiceTests {

    private static final Duration TIMEOUT = Duration.ofSeconds(1);
    private static final Duration CACHE_TTL = Duration.ofSeconds(10);

    @Test
    void reportsOnlineWhenAuthAndWorldServersAreReachable() {
        GameServerStatusService.PortProbe probe = mock(GameServerStatusService.PortProbe.class);
        when(probe.isReachable("ac-authserver", 3724, TIMEOUT)).thenReturn(true);
        when(probe.isReachable("ac-worldserver", 8085, TIMEOUT)).thenReturn(true);
        GameServerStatusService service = service(probe, new AtomicLong());

        assertThat(service.isOnline()).isTrue();

        verify(probe).isReachable("ac-authserver", 3724, TIMEOUT);
        verify(probe).isReachable("ac-worldserver", 8085, TIMEOUT);
    }

    @Test
    void reportsOfflineWhenAuthServerIsNotReachable() {
        GameServerStatusService.PortProbe probe = mock(GameServerStatusService.PortProbe.class);
        when(probe.isReachable("ac-authserver", 3724, TIMEOUT)).thenReturn(false);
        GameServerStatusService service = service(probe, new AtomicLong());

        assertThat(service.isOnline()).isFalse();

        verify(probe).isReachable("ac-authserver", 3724, TIMEOUT);
        verifyNoMoreInteractions(probe);
    }

    @Test
    void cachesTheResultUntilTheConfiguredTtlExpires() {
        GameServerStatusService.PortProbe probe = mock(GameServerStatusService.PortProbe.class);
        when(probe.isReachable("ac-authserver", 3724, TIMEOUT)).thenReturn(true);
        when(probe.isReachable("ac-worldserver", 8085, TIMEOUT))
                .thenReturn(true)
                .thenReturn(false);
        AtomicLong nanoTime = new AtomicLong();
        GameServerStatusService service = service(probe, nanoTime);

        assertThat(service.isOnline()).isTrue();
        assertThat(service.isOnline()).isTrue();

        nanoTime.set(CACHE_TTL.toNanos());
        assertThat(service.isOnline()).isFalse();
    }

    private static GameServerStatusService service(
            GameServerStatusService.PortProbe probe,
            AtomicLong nanoTime
    ) {
        ServerStatusProperties properties = new ServerStatusProperties(
                "ac-authserver",
                3724,
                "ac-worldserver",
                8085,
                TIMEOUT,
                CACHE_TTL
        );
        return new GameServerStatusService(properties, probe, nanoTime::get);
    }
}
