package dev.kmfg.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayersOnlyCompass {
    private static final ChatColor[] COLORS = {
            ChatColor.AQUA,
            ChatColor.BLACK,
            ChatColor.DARK_BLUE,
            ChatColor.MAGIC,
            ChatColor.DARK_GREEN,
            ChatColor.GREEN,
            ChatColor.GOLD,
            ChatColor.GRAY,
            ChatColor.RED,
    };

    private static final int COMPASS_SLOTS = 40;
    private static final int SLOT_DEGREES = 360 / COMPASS_SLOTS;

    private static final int NORTH_SLOT = 20;
    private static final int EAST_SLOT = 30;
    private static final int SOUTH_SLOT = 0;
    private static final int WEST_SLOT = 10;

    private static final Map<UUID, ChatColor> COLOR_ASSIGNMENTS = new HashMap<>();
    private final Player owner;
    private final BossBar bossBar;

    public PlayersOnlyCompass(Player owner) {
        this.owner = owner;
        this.bossBar = Bukkit.createBossBar(
                "Loading compass...",
                BarColor.BLUE,
                BarStyle.SOLID);
        this.bossBar.addPlayer(owner);
        this.bossBar.setVisible(true);
        this.bossBar.setProgress(1.0);

        ChatColor assignedColor = getColorForUUID(owner.getUniqueId());
        owner.sendMessage("[PlayerCompass]: " + ChatColor.GRAY + "Your compass marker color is: " + assignedColor + "X"
                + ChatColor.RESET);
    }

    public BossBar getBar() {
        return bossBar;
    }

    public void updateCompass() {
        CompassSlot[] ring = buildBaseCompassRing();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(owner)) {
                continue;
            }
            int slotIndex = getSlotIndex(owner.getLocation(), p.getLocation());
            ring[slotIndex].occupant = p.getUniqueId();
        }

        int facingSlot = normalizeSlotIndex(owner.getLocation().getYaw());
        int startIndex = facingSlot - 10;
        String slice = buildSlice(ring, startIndex, 21);

        bossBar.setTitle(slice);
    }

    /**
     * Creates a base array of CompassSlot, marking only the cardinal directions.
     * All other slots are initially empty.
     */
    private CompassSlot[] buildBaseCompassRing() {
        CompassSlot[] ring = new CompassSlot[COMPASS_SLOTS];
        for (int i = 0; i < COMPASS_SLOTS; i++) {
            ring[i] = new CompassSlot();
        }
        ring[NORTH_SLOT].cardinal = 'N';
        ring[EAST_SLOT].cardinal = 'E';
        ring[SOUTH_SLOT].cardinal = 'S';
        ring[WEST_SLOT].cardinal = 'W';

        return ring;
    }

    /**
     * Determines which slot index a target is in, relative to the source.
     * For example, if the target is exactly east, we return 30, etc...
     */
    private int getSlotIndex(Location source, Location target) {
        Vector direction = target.toVector().subtract(source.toVector());
        source.setDirection(direction);

        return normalizeSlotIndex(source.getYaw());
    }

    /**
     * Normalizes a yaw to an integer [0..39], each representing 9 degrees.
     */
    private int normalizeSlotIndex(float yaw) {
        int slot = Math.round((yaw + 360) % 360 / SLOT_DEGREES);
        return slot % COMPASS_SLOTS;
    }

    /**
     * Builds a slice of length 'sliceLength' from the ring, starting at
     * 'startIndex' (which can be negative).
     * Wraps around if needed. Each slot is turned into a 1-character (or
     * multi-character) string:
     * - If there's a cardinal direction (N/E/S/W), color it gold.
     * - If there's an occupant, color it based on occupant's UUID.
     * - Otherwise, it's just '⬟'.
     */
    private String buildSlice(CompassSlot[] ring, int startIndex, int sliceLength) {
        // Fix up the startIndex if negative or out of range
        while (startIndex < 0) {
            startIndex += COMPASS_SLOTS;
        }
        StringBuilder sb = new StringBuilder(sliceLength * 3);

        for (int i = 0; i < sliceLength; i++) {
            int actualIndex = (startIndex + i) % COMPASS_SLOTS;
            CompassSlot slot = ring[actualIndex];

            if (slot.occupant != null) {
                ChatColor color = getColorForUUID(slot.occupant);
                sb.append(color).append('X').append(ChatColor.RESET);
            } else if (slot.cardinal != '\0') {
                sb.append(ChatColor.GOLD).append(slot.cardinal).append(ChatColor.RESET);
            } else {
                sb.append('⬟');
            }
        }
        return sb.toString();
    }

    /**
     * Always returns the same color for a given UUID, based on the UUID's hashCode
     * mod the size of the COLORS array. We never remove assignments, so the same
     * player keeps the same color for the entire server session.
     *
     * If you want the same color across restarts, store and load COLOR_ASSIGNMENTS
     * from a file or database in your plugin's onEnable/onDisable.
     */
    public static ChatColor getColorForUUID(UUID uuid) {
        if (COLOR_ASSIGNMENTS.containsKey(uuid)) {
            return COLOR_ASSIGNMENTS.get(uuid);
        }
        int index = Math.abs(uuid.hashCode()) % COLORS.length;
        ChatColor color = COLORS[index];
        COLOR_ASSIGNMENTS.put(uuid, color);
        return color;
    }

    /**
     * Do NOT call this unless you truly want to remove a player's color mapping.
     * It is not memory expensive to keep this map. However, some may see it as a
     * memory leak for large or long running server which switch between many
     * players.
     */
    public static void clearPlayerColor(UUID uuid) {
        COLOR_ASSIGNMENTS.remove(uuid);
    }

    private static class CompassSlot {
        char cardinal = '\0'; // 'N', 'E', 'S', or 'W' if cardinal
        UUID occupant = null; // which player (if any) occupies this slot
    }
}
