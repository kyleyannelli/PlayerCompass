package dev.kmfg.plugin;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerCompass extends JavaPlugin {
    private static PlayerCompass instance;

    private CompassMoveEvent moveEvent;

    @Override
    public void onEnable() {
        instance = this;

        moveEvent = new CompassMoveEvent();
        moveEvent.runTaskTimer(this, 0, 1);

        this.getCommand("pcc").setExecutor(new PlayerCompassCommand());

        Bukkit.getLogger().log(Level.INFO, "[PlayerCompass]: Plugin enabled, rendering compasses for everyone.");
    }

    @Override
    public void onDisable() {
        if (moveEvent == null)
            return;

        moveEvent.cancel();
        moveEvent.clearAllCompasses();
        moveEvent = null;

        Bukkit.getLogger().log(Level.INFO, "[PlayerCompass]: Plugin disabled, clearing compasses for everyone.");
    }

    public static PlayerCompass getInstance() {
        return instance;
    }
}
