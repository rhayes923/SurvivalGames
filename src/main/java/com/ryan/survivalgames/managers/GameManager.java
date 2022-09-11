package com.ryan.survivalgames.managers;

import com.ryan.survivalgames.SurvivalGames;
import com.ryan.survivalgames.listeners.GameState;
import com.ryan.survivalgames.listeners.WaitingState;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GameManager {

    final SurvivalGames survivalGames;
    PlayerManager playerManager;
    boolean isStarted = false;
    GameState gameState;
    World world;
    World mainWorld = SurvivalGames.getInstance().getServer().getWorld("world");
    WorldBorder worldBorder;
    int capacity;
    List<Location> spawns = new ArrayList<>();

    public GameManager(SurvivalGames plugin) {
        this.survivalGames = plugin;
    }

    public void setPlayerManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public void setGameState(GameState gameState) {
        HandlerList.unregisterAll(this.gameState);
        this.gameState = gameState;
        survivalGames.getServer().getPluginManager().registerEvents(gameState, survivalGames);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void startGame(Player player, String map) {
        if (!isStarted) {
            File file = new File(survivalGames.getServer().getWorldContainer(), map);
            if (file.exists() && file.isDirectory()) {
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    if (f.getName().contains("level.dat")) {
                        player.sendMessage(getChatPrefix() + ChatColor.YELLOW + " World named " + map + " found, loading...");
                        boolean valid = createWorld(map);
                        if (valid) {
                            isStarted = true;
                            setGameState(new WaitingState());
                            Bukkit.broadcastMessage(getChatPrefix() + ChatColor.YELLOW + " A game has started! Do /sg join to join!");
                        } else {
                            player.sendMessage(getChatPrefix() + ChatColor.RED + " That is not a valid world!");
                            Bukkit.unloadWorld(map, false);
                        }
                        return;
                    }
                }
            }
            player.sendMessage(getChatPrefix() + ChatColor.RED + " Could not find a world named " + map + "!");
        } else {
            player.sendMessage(getChatPrefix() + ChatColor.RED + " A game has already started!");
        }
    }

    public String getChatPrefix() {
        return ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "SurvivalGames" + ChatColor.DARK_GRAY + "]";
    }

    public String getCapacitySuffix() {
        return "(" + ChatColor.GREEN + playerManager.getPlayers().size() + ChatColor.YELLOW + "/" + ChatColor.GREEN + capacity + ChatColor.YELLOW + ")";
    }

    public void setScoreboard(Player player) {
        if (Bukkit.getScoreboardManager() != null) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("Scoreboard", Criteria.DUMMY, ChatColor.AQUA + "" + ChatColor.BOLD + "Survival Games");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            objective.getScore("").setScore(6);
            objective.getScore(ChatColor.GREEN + "Next Event").setScore(5);

            Team eventText = scoreboard.registerNewTeam("event");
            eventText.addEntry(" ");
            eventText.setPrefix(ChatColor.RED + "Chest Refill: 5:00");
            objective.getScore(" ").setScore(4);

            objective.getScore("  ").setScore(3);

            Team playersText = scoreboard.registerNewTeam("players");
            playersText.addEntry("   ");
            playersText.setPrefix(ChatColor.GOLD + "Players: " + playerManager.getAlivePlayers().size());
            objective.getScore("   ").setScore(2);

            Team killsText = scoreboard.registerNewTeam("kills");
            killsText.addEntry("    ");
            killsText.setPrefix(ChatColor.GOLD + "Kills: 0");
            objective.getScore("    ").setScore(1);

            player.setScoreboard(scoreboard);
        }
    }

    public void updateScoreboard(Player player, String event, String time, int kills) {
        Scoreboard scoreboard = player.getScoreboard();
        Optional.ofNullable(scoreboard.getTeam("event")).ifPresent(team -> team.setPrefix(ChatColor.RED + event + ": " + time));
        Optional.ofNullable(scoreboard.getTeam("players")).ifPresent(team -> team.setPrefix(ChatColor.GOLD + "Players: " + playerManager.getAlivePlayers().size()));
        Optional.ofNullable(scoreboard.getTeam("kills")).ifPresent(team -> team.setPrefix(ChatColor.GOLD + "Kills: " + kills));
    }

    public String getFormattedTime(int time) {
        return time / 60 + ":" + (time % 60 == 0 ? "00" : time % 60 < 10 ? "0" + time % 60 : time % 60);
    }

    public boolean createWorld(String name) {
        world = Bukkit.createWorld(new WorldCreator(name));
        if (world != null) {
            world.setAutoSave(false);
            world.getWorldBorder().reset();
            Chunk spawnChunk = world.getSpawnLocation().getChunk();
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    Chunk chunk = world.getChunkAt(spawnChunk.getX() + x, spawnChunk.getZ() + z);
                    for (BlockState block : chunk.getTileEntities()) {
                        if (block instanceof Sign && ((Sign) block).getLine(0).equalsIgnoreCase("spawn")) {
                            spawns.add(block.getLocation());
                            block.getBlock().setType(Material.AIR);
                        }
                    }
                }
            }
            capacity = spawns.size();
        }
        return capacity > 1;
    }

    public void updateBarriers(Location loc, Material material) {
        int[] offset = {1, -1, 0, 0};
        int z = offset.length - 1;
        for (int y = 0; y < 2; y++) {
            for (int i : offset) {
                loc.clone().add(i, y, offset[z--]).getBlock().setType(material);
            }
            z = offset.length - 1;
        }
        loc.clone().add(0, 2, 0).getBlock().setType(material);
    }

    public World getSurvivalGamesWorld() {
        return world;
    }

    public World getMainWorld() {
        return mainWorld;
    }

    public void setWorldBorder(WorldBorder border) {
        worldBorder = border;
    }

    public WorldBorder getWorldBorder() {
        return worldBorder;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<Location> getSpawns() {
        return spawns;
    }

    public void unload() {
        if (getSurvivalGamesWorld() != null) {
            for (Player player : getSurvivalGamesWorld().getPlayers()) {
                player.teleport(getMainWorld().getSpawnLocation());
                playerManager.resetPlayer(player);
            }
            Bukkit.unloadWorld(getSurvivalGamesWorld(), false);
        }
    }

    public void reset() {
        isStarted = false;
        gameState = null;
        world = null;
        worldBorder = null;
        capacity = 0;
        spawns = new ArrayList<>();
        HandlerList.unregisterAll(survivalGames);
    }
}