package dev.kmfg.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;

public class PlayersOnlyCompass {
    private static final char HOUSE_CHARACTER = '⌂';

    private static final String CHARACTERS = "±ØCB¥DTY█AQPRXFVUO%Æ@&J▒G$MZ#KIH£L";

    private static final ChatColor[] COLORS = {
            ChatColor.AQUA,
            ChatColor.BLACK,
            ChatColor.DARK_BLUE,
            ChatColor.DARK_GREEN,
            ChatColor.GREEN,
            ChatColor.GOLD,
            ChatColor.DARK_GRAY,
            ChatColor.GRAY,
            ChatColor.RED,
    };

    private static List<CharacterColorSet> CHARACTER_POOL = initPool();

    private static List<CharacterColorSet> initPool() {
        List<CharacterColorSet> pool = new ArrayList<CharacterColorSet>();
        for (int caIdx = 0; caIdx < CHARACTERS.length(); caIdx++) {
            for (int coIdx = 0; coIdx < COLORS.length; coIdx++) {
                pool.add(new CharacterColorSet(CHARACTERS.charAt(caIdx), COLORS[coIdx]));
            }
        }
        return pool;
    }

    private static final double MAX_RANGE = 1000;
    private static final double MAX_RANGE_SQUARED = MAX_RANGE * MAX_RANGE;

    private static final int COMPASS_SLOTS = 40;
    private static final int SLOT_DEGREES = 360 / COMPASS_SLOTS;

    private static final int NORTH_SLOT = 20;
    private static final int EAST_SLOT = 30;
    private static final int SOUTH_SLOT = 0;
    private static final int WEST_SLOT = 10;

    private static final Map<UUID, CharacterColorSet> COLOR_ASSIGNMENTS = new HashMap<>();
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

        final CharacterColorSet ccSet = getColorForUUID(owner.getUniqueId());
        final String ccSetStr = ccSet.toString();
        owner.sendMessage(
                "[PlayerCompass]: " + ChatColor.GRAY + "Your compass marker is: " + ccSetStr);
        String publicMessage = ChatColor.YELLOW + "[PlayerCompass]: " + owner.getName()
                + " has joined with marker " + ccSetStr + " !";
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!player.equals(owner)) {
                player.sendMessage(publicMessage);
            }
        });
    }

    public BossBar getBar() {
        return bossBar;
    }

    public void updateCompass() {
        CompassSlot[] ring = buildBaseCompassRing();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (dontPopulatePlayer(p)) {
                continue;
            }
            final int slotIndex = getSlotIndex(owner.getLocation(), p.getLocation());
            ring[slotIndex].occupant = p.getUniqueId();
        }

        final boolean isOverworld = owner.getWorld().getEnvironment() == World.Environment.NORMAL;

        if (isOverworld) {
            final Location respawnLocation = owner.getRespawnLocation();
            final int houseSlotIndex = getSlotIndex(owner.getLocation(),
                    respawnLocation == null ? owner.getWorld().getSpawnLocation() : respawnLocation);
            ring[houseSlotIndex].cardinal = HOUSE_CHARACTER;
        }

        int facingSlot = normalizeSlotIndex(owner.getLocation().getYaw());
        int startIndex = facingSlot - 10;
        String slice = buildSlice(ring, startIndex, 21);

        bossBar.setTitle(slice);
    }

    /**
     * Checks if we should populate the player or not based on
     * - Being in the same world.
     * - Not being yourself.
     * - Being in range.
     */
    private boolean dontPopulatePlayer(Player other) {
        if (!other.getWorld().equals(owner.getWorld())) {
            return true;
        }

        Location otherLocation = other.getLocation();
        Location ownerLocation = owner.getLocation();

        double dx = otherLocation.getX() - ownerLocation.getX();
        double dz = otherLocation.getZ() - ownerLocation.getZ();
        double distanceSquared = dx * dx + dz * dz;

        return distanceSquared > MAX_RANGE_SQUARED || other.equals(owner);
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
            final int actualIndex = (startIndex + i) % COMPASS_SLOTS;
            final CompassSlot slot = ring[actualIndex];
            final boolean isHouse = slot.cardinal == HOUSE_CHARACTER;

            if (isHouse) {
                sb.append(ChatColor.RED).append(slot.cardinal).append(ChatColor.RESET);
            } else if (slot.occupant != null) {
                final CharacterColorSet ccSet = getColorForUUID(slot.occupant);
                sb.append(ccSet.color).append(ccSet.value).append(ChatColor.RESET);
            } else if (slot.cardinal != '\0') {
                sb.append(ChatColor.GOLD).append(slot.cardinal).append(ChatColor.RESET);
            } else {
                sb.append('⬟');
            }
        }
        return sb.toString();
    }

    /**
     * Gets CharacterColorSet for UUID based on pool availability.
     * Persisent only for server session.
     */
    public static CharacterColorSet getColorForUUID(UUID uuid) {
        if (COLOR_ASSIGNMENTS.containsKey(uuid)) {
            return COLOR_ASSIGNMENTS.get(uuid);
        } else if (CHARACTER_POOL.isEmpty()) {
            Bukkit.getLogger().log(Level.WARNING,
                    "[PlayerCompass] WARNING: The number of players have exceeded the marker pool size. The pool is refilling, there will be overlaps!");
            CHARACTER_POOL = initPool();
        }

        final int poolIdx = Math.abs(uuid.hashCode()) % CHARACTER_POOL.size();
        final CharacterColorSet ccSet = CHARACTER_POOL.get(poolIdx);
        CHARACTER_POOL.remove(poolIdx);
        COLOR_ASSIGNMENTS.put(uuid, ccSet);
        return ccSet;
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

    public static class CharacterColorSet {
        final char value;
        final ChatColor color;

        CharacterColorSet(char value, ChatColor color) {
            this.value = value;
            this.color = color;
        }

        @Override
        public String toString() {
            return "" + this.color + this.value + ChatColor.RESET;
        }
    }
}
