package com.ryan.survivalgames.listeners;

import com.ryan.survivalgames.managers.GameManager;
import com.ryan.survivalgames.managers.PlayerManager;
import com.ryan.survivalgames.SurvivalGames;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class EndState implements GameState {

    final SurvivalGames survivalGames = SurvivalGames.getInstance();
    final GameManager gameManager = SurvivalGames.getGameManager();
    final PlayerManager playerManager = SurvivalGames.getPlayerManager();
    int timer = 15;
    Player winner;

    @Override
    public GameState nextState() {
        return null;
    }

    public EndState() {
        winner = Bukkit.getPlayer(playerManager.getAlivePlayers().get(0));
        final Map<String, Integer> killResults = playerManager.getSortedKills();
        if (playerManager.getAlivePlayers().size() > 0) {
            for (Map.Entry<String, Integer> e : killResults.entrySet()) {
                Player player = Bukkit.getPlayer(e.getKey());
                if (player != null) {
                    if (playerManager.getAlivePlayers().contains(player.getUniqueId())) {
                        winner = Bukkit.getPlayer(e.getKey());
                        break;
                    }
                }
            }
        }

        for (UUID uuid : playerManager.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendTitle(ChatColor.GOLD + "WINNER", ChatColor.GOLD + winner.getName(), 20, 260, 20);
            }
        }

        chatResults(killResults, winner);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (timer > 2) {
                    Firework firework = (Firework) winner.getWorld().spawnEntity(winner.getLocation(), EntityType.FIREWORK);
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.setPower(2);
                    meta.addEffect(FireworkEffect.builder().withColor(Color.ORANGE).flicker(true).build());
                    firework.setFireworkMeta(meta);
                } else if (timer == 0) {
                    for (UUID uuid : playerManager.getPlayers()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            playerManager.resetPlayer(player);
                            player.teleport(gameManager.getMainWorld().getSpawnLocation());
                        }
                    }
                    playerManager.getPlayers().clear();
                    playerManager.getAlivePlayers().clear();
                    gameManager.unload();
                    gameManager.reset();
                    playerManager.reset();
                    this.cancel();
                }
                timer--;
            }
        }.runTaskTimer(survivalGames, 0, 20);
    }

    public void chatResults(Map<String, Integer> results, Player winner) {
        String[] top = new String[3];
        Map.Entry<String, Integer> iterator;
        for (int i = 0; i < 3; i++) {
            if (results.entrySet().iterator().hasNext()) {
                iterator = results.entrySet().iterator().next();
                top[i] = iterator.getKey() + " - " + iterator.getValue() + (iterator.getValue() == 1 ? " Kill" : " Kills");
                results.remove(iterator.getKey());
            }
        }
        for (UUID uuid : playerManager.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "---------------------------");
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "WINNER: " + winner.getName());
                player.sendMessage("");
                if (top[0] != null) player.sendMessage(ChatColor.YELLOW + "1st Killer: " + top[0]);
                if (top[1] != null) player.sendMessage(ChatColor.GOLD + "2nd Killer: " + top[1]);
                if (top[2] != null) player.sendMessage(ChatColor.RED + "3rd Killer: " + top[2]);
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "---------------------------");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (playerManager.getPlayers().contains(e.getPlayer().getUniqueId())) {
            Player player = Bukkit.getPlayer(e.getPlayer().getUniqueId());
            if (player != null) {
                playerManager.getPlayers().remove(player.getUniqueId());
                playerManager.getAlivePlayers().remove(player.getUniqueId());
                playerManager.resetPlayer(e.getPlayer());
                e.getPlayer().teleport(gameManager.getMainWorld().getSpawnLocation());
                e.setQuitMessage("");
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        e.setCancelled(true);
    }
}
