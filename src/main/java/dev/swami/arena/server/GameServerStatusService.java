package dev.swami.arena.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.function.LongSupplier;

@Service
class GameServerStatusService {

    private final ServerStatusProperties properties;
    private final PortProbe portProbe;
    private final LongSupplier nanoTime;
    private volatile CachedStatus cachedStatus;

    @Autowired
    GameServerStatusService(ServerStatusProperties properties) {
        this(properties, GameServerStatusService::canConnect, System::nanoTime);
    }

    GameServerStatusService(
            ServerStatusProperties properties,
            PortProbe portProbe,
            LongSupplier nanoTime
    ) {
        this.properties = properties;
        this.portProbe = portProbe;
        this.nanoTime = nanoTime;
    }

    boolean isOnline() {
        long now = nanoTime.getAsLong();
        CachedStatus current = cachedStatus;
        if (isFresh(current, now)) {
            return current.online();
        }

        synchronized (this) {
            current = cachedStatus;
            if (isFresh(current, now)) {
                return current.online();
            }

            boolean online = portProbe.isReachable(
                    properties.authHost(),
                    properties.authPort(),
                    properties.connectTimeout()
            ) && portProbe.isReachable(
                    properties.worldHost(),
                    properties.worldPort(),
                    properties.connectTimeout()
            );
            cachedStatus = new CachedStatus(online, now);
            return online;
        }
    }

    private boolean isFresh(CachedStatus status, long now) {
        return status != null
                && now - status.checkedAtNanos() < properties.cacheTtl().toNanos();
    }

    private static boolean canConnect(String host, int port, Duration timeout) {
        int timeoutMillis = (int) Math.min(timeout.toMillis(), Integer.MAX_VALUE);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    @FunctionalInterface
    interface PortProbe {
        boolean isReachable(String host, int port, Duration timeout);
    }

    private record CachedStatus(boolean online, long checkedAtNanos) {
    }
}
