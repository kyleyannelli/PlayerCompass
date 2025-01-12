package dev.kmfg.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CompassMoveEvent extends BukkitRunnable {
    private final Map<UUID, PlayersOnlyCompass> playerCompasses = new HashMap<>();

    @Override
    public void run() {
        if (this.isCancelled()) {
            clearAllCompasses();
            return;
        }

        updateOnlinePlayerCompasses();
        clearStalePlayers();
    }

    private void updateOnlinePlayerCompasses() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            final PlayersOnlyCompass compass = playerCompasses.computeIfAbsent(
                    player.getUniqueId(),
                    uuid -> {
                        return new PlayersOnlyCompass(player);
                    });
            compass.updateCompass();
        }
    }

    private void clearStalePlayers() {
        Iterator<UUID> iterator = playerCompasses.keySet().iterator();
        while (iterator.hasNext()) {
            final UUID uuid = iterator.next();
            final Player player = Bukkit.getPlayer(uuid);
            final boolean isStale = player == null || !player.isOnline();
            if (isStale) {
                iterator.remove();
                // PlayersOnlyCompass.clearPlayerColor(uuid); hmmmm...
            }
        }
    }

    /**
     * Removes all compasses from all players.
     */
    public void clearAllCompasses() {
        Iterator<PlayersOnlyCompass> iterator = playerCompasses.values().iterator();
        while (iterator.hasNext()) {
            final PlayersOnlyCompass compass = iterator.next();
            compass.getBar().removeAll();
            iterator.remove();
        }
    }
}
