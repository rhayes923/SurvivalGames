package com.ryan.survivalgames.listeners;

import com.ryan.survivalgames.managers.GameManager;
import com.ryan.survivalgames.managers.PlayerManager;
import com.ryan.survivalgames.SurvivalGames;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.UUID;

public class PreGameState implements GameState {

    final SurvivalGames survivalGames = SurvivalGames.getInstance();
    final GameManager gameManager = SurvivalGames.getGameManager();
    final PlayerManager playerManager = SurvivalGames.getPlayerManager();

    @Override
    public GameState nextState() {
        return new ActiveState();
    }

    public PreGameState() {

        new BukkitRunnable() {
            int timer = 15;
            boolean teleported = false;

            @Override
            public void run() {
                if (gameManager.getSurvivalGamesWorld().getSpawnLocation().isWorldLoaded()) {
                    if (!teleported) {
                        for (UUID uuid : playerManager.getPlayers()) {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null) {
                                Location loc = gameManager.getSpawns().get(new Random().nextInt(gameManager.getSpawns().size()));
                                gameManager.getSpawns().remove(loc);
                                player.teleport(loc.clone().add(0.5, 0, 0.5));
                                gameManager.updateBarriers(loc, Material.BARRIER);
                                playerManager.resetPlayer(player);
                                player.sendTitle(ChatColor.AQUA + "Survival Games", ChatColor.YELLOW + "Last Player Standing Wins!", 60, 100, 60);
                            }
                        }
                        teleported = true;
                    } else {
                        if (timer <= 0) {
                            this.cancel();
                            for (UUID uuid : playerManager.getPlayers()) {
                                Player player = Bukkit.getPlayer(uuid);
                                if (player != null) {
                                    gameManager.updateBarriers(player.getLocation(), Material.AIR);
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1F, 1F);
                                    player.sendMessage(gameManager.getChatPrefix() + ChatColor.YELLOW + " The game has started!");
                                }
                            }
                            gameManager.setGameState(nextState());
                            return;
                        }
                        if (timer == 15 || timer <= 5) {
                            for (UUID uuid : playerManager.getPlayers()) {
                                Player player = Bukkit.getPlayer(uuid);
                                if (player != null) {
                                    player.sendMessage(gameManager.getChatPrefix() + ChatColor.YELLOW + " The game will start in " + timer + (timer == 1 ? " second!" : " seconds!"));
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1F, 1F);
                                }
                            }
                        }
                        timer--;
                    }
                }
            }
        }.runTaskTimer(survivalGames, 0, 20);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void worldInit(WorldInitEvent e) {
        gameManager.getSurvivalGamesWorld().setKeepSpawnInMemory(false);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (playerManager.getPlayers().contains(e.getPlayer().getUniqueId())) {
            e.setQuitMessage("");
            playerManager.removePlayer(e.getPlayer());
        }
    }
}
