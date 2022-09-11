package com.ryan.survivalgames;

import com.ryan.survivalgames.commands.SurvivalGamesCommand;
import com.ryan.survivalgames.managers.GameManager;
import com.ryan.survivalgames.managers.PlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SurvivalGames extends JavaPlugin {

    static SurvivalGames instance;
    static GameManager gameManager;
    static PlayerManager playerManager;

    @Override
    public void onEnable() {
        instance = this;
        gameManager = new GameManager(this);
        playerManager = new PlayerManager(this);
        gameManager.setPlayerManager(playerManager);
        getCommand("sg").setExecutor(new SurvivalGamesCommand());
    }

    @Override
    public void onDisable() {
        gameManager.unload();
    }

    public static SurvivalGames getInstance() {
        return instance;
    }

    public static GameManager getGameManager() {
        return gameManager;
    }

    public static PlayerManager getPlayerManager() {
        return playerManager;
    }
}
