package com.ryan.survivalgames.commands;

import com.ryan.survivalgames.managers.GameManager;
import com.ryan.survivalgames.SurvivalGames;
import com.ryan.survivalgames.listeners.WaitingState;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SurvivalGamesCommand implements CommandExecutor {

    static final GameManager gameManager = SurvivalGames.getGameManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (label.equals("sg") && args.length > 0) {
                switch (args[0]) {
                    case "join":
                        SurvivalGames.getPlayerManager().addPlayer(player);
                        break;
                    case "leave":
                        SurvivalGames.getPlayerManager().removePlayer(player);
                        break;
                    case "start":
                        if (args.length > 1) {
                            gameManager.startGame(player, args[1]);
                        } else {
                            player.sendMessage(gameManager.getChatPrefix() + ChatColor.RED + " You must specify a world name!");
                        }
                        break;
                    case "forcestart":
                        WaitingState.forceStart();
                        break;
                }
                return true;
            }
        }
        return false;
    }
}
