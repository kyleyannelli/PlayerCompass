package dev.kmfg.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCompassCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "Compass Marker Colors for Online Players:");
        for (Player player : Bukkit.getOnlinePlayers()) {
            ChatColor color = PlayersOnlyCompass.getColorForUUID(player.getUniqueId());
            sender.sendMessage(ChatColor.GRAY + player.getName() + ": " + color + "X" + ChatColor.RESET);
        }
        return true;
    }
}
