package com.ryan.survivalgames.managers;

import com.ryan.survivalgames.SurvivalGames;
import com.ryan.survivalgames.listeners.ActiveState;
import com.ryan.survivalgames.listeners.PreGameState;
import com.ryan.survivalgames.listeners.WaitingState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class PlayerManager {

    SurvivalGames survivalGames;
    GameManager gameManager = SurvivalGames.getGameManager();
    List<UUID> players = new ArrayList<>();
    List<UUID> alivePlayers = new ArrayList<>();
    Map<String, Integer> kills = new HashMap<>();

    public PlayerManager(SurvivalGames plugin) {
        this.survivalGames = plugin;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public List<UUID> getAlivePlayers() {
        return alivePlayers;
    }

    public Map<String, Integer> getKills() {
        return kills;
    }

    public Map<String, Integer> getSortedKills() {
        Map<String, Integer> sorted = new LinkedHashMap<>();
        kills.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEach(x ->
                sorted.put(x.getKey(), x.getValue()));
        return sorted;
    }

    public void resetPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.showPlayer(survivalGames, player);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setLevel(0);
        player.setExp(0);
        if (Bukkit.getScoreboardManager() != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void addPlayer(Player player) {
        if (gameManager.getGameState() instanceof WaitingState) {
            if (getPlayers().size() < gameManager.getCapacity()) {
                if (!getPlayers().contains(player.getUniqueId())) {
                    getPlayers().add(player.getUniqueId());
                    if (Bukkit.getScoreboardManager() != null) {
                        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    }
                    Bukkit.broadcastMessage(gameManager.getChatPrefix() + ChatColor.GREEN + " " + player.getName() + ChatColor.YELLOW + " has joined! " + gameManager.getCapacitySuffix());
                } else {
                    player.sendMessage(gameManager.getChatPrefix() + ChatColor.RED + " You have already joined!");
                }
            } else {
                player.sendMessage(gameManager.getChatPrefix() + ChatColor.RED + " The game is full!");
            }
        } else {
            player.sendMessage(gameManager.getChatPrefix() + ChatColor.RED + " You cannot join right now!");
        }
    }

    public void removePlayer(Player player) {
        if (gameManager.getGameState() instanceof WaitingState) {
            if (getPlayers().contains(player.getUniqueId())) {
                getPlayers().remove(player.getUniqueId());
                Bukkit.broadcastMessage(gameManager.getChatPrefix() + ChatColor.GREEN + " " + player.getName() + ChatColor.YELLOW + " has left! " + gameManager.getCapacitySuffix());
                return;
            }
        } else if (gameManager.getGameState() instanceof PreGameState) {
            if (getPlayers().contains(player.getUniqueId())) {
                player.damage(20);
                player.getWorld().strikeLightningEffect(player.getLocation());
                gameManager.updateBarriers(player.getLocation(), Material.AIR);
                getPlayers().remove(player.getUniqueId());
                player.teleport(gameManager.getMainWorld().getSpawnLocation());
                resetPlayer(player);
                return;
            }
        } else if (gameManager.getGameState() instanceof ActiveState) {
            if (getPlayers().contains(player.getUniqueId())) {
                if (getAlivePlayers().contains(player.getUniqueId())) {
                    player.damage(20);
                    player.getWorld().strikeLightningEffect(player.getLocation());
                    getAlivePlayers().remove(player.getUniqueId());
                }
                getPlayers().remove(player.getUniqueId());
                player.teleport(gameManager.getMainWorld().getSpawnLocation());
                resetPlayer(player);
                return;
            }
        }
        player.sendMessage(gameManager.getChatPrefix() + ChatColor.RED + " You are not part of a game!");
    }

    public void reset() {
        players.clear();
        alivePlayers.clear();
        kills.clear();
    }
}
