package com.ryan.survivalgames.listeners;

import com.ryan.survivalgames.managers.GameManager;
import com.ryan.survivalgames.managers.PlayerManager;
import com.ryan.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class WaitingState implements GameState {

    final SurvivalGames survivalGames = SurvivalGames.getInstance();
    final GameManager gameManager = SurvivalGames.getGameManager();
    final PlayerManager playerManager = SurvivalGames.getPlayerManager();
    static boolean forceStart;

    @Override
    public GameState nextState() {
        return new PreGameState();
    }

    public WaitingState() {
        forceStart = false;

        new BukkitRunnable() {
            int timer = 10;
            boolean maxReached = false;
            boolean countdown = false;

            @Override
            public void run() {
                maxReached = playerManager.getPlayers().size() == gameManager.getCapacity();
                if (forceStart) {
                    timer = 0;
                    maxReached = true;
                }
                if (maxReached) {
                    if (timer <= 0) {
                        this.cancel();
                        gameManager.setGameState(nextState());
                        return;
                    }
                    countdown = true;
                    Bukkit.broadcastMessage(gameManager.getChatPrefix() + ChatColor.YELLOW + " Teleporting in " + timer-- + " seconds!");
                    for (UUID uuid : playerManager.getPlayers()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1F, 1F);
                        }
                    }

                } else if (countdown) {
                    Bukkit.broadcastMessage(gameManager.getChatPrefix() + ChatColor.YELLOW + " Countdown canceled! Waiting for more players!");
                    countdown = false;
                    timer = 10;
                }
            }
        }.runTaskTimer(survivalGames, 0, 20);
    }

    public static void forceStart() {
        forceStart = true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerManager.removePlayer(e.getPlayer());
    }
}
