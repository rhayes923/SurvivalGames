package com.ryan.survivalgames.listeners;

import org.bukkit.event.Listener;

public interface GameState extends Listener {
    GameState nextState();
}
