package dev.kmfg.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.kmfg.plugin.PlayersOnlyCompass.CharacterColorSet;

public class PlayerCompassCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "[PlayerCompass]: Compass Markers for Online Players:");
        for (Player player : Bukkit.getOnlinePlayers()) {
            CharacterColorSet ccSet = PlayersOnlyCompass.getColorForUUID(player.getUniqueId());
            sender.sendMessage("\t" + ChatColor.GRAY + player.getName() + ": " + ccSet.toString() + ChatColor.RESET);
        }
        return true;
    }
}
