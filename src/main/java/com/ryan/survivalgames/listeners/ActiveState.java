package com.ryan.survivalgames.listeners;

import com.ryan.survivalgames.SurvivalGames;
import com.ryan.survivalgames.items.ChestItem;
import com.ryan.survivalgames.managers.GameManager;
import com.ryan.survivalgames.managers.PlayerManager;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ActiveState implements GameState {

    final SurvivalGames survivalGames = SurvivalGames.getInstance();
    final GameManager gameManager = SurvivalGames.getGameManager();
    final PlayerManager playerManager = SurvivalGames.getPlayerManager();
    final Set<Chest> checkedChests = new HashSet<>();
    int timer = 300;
    String nextEvent = "Chest Refill";
    final Random random = new Random();

    @Override
    public GameState nextState() {
        return new EndState();
    }

    public ActiveState() {

        for (UUID uuid : playerManager.getPlayers()) {
            playerManager.getAlivePlayers().add(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                playerManager.getKills().put(player.getName(), 0);
                gameManager.setScoreboard(player);
            }
        }

        gameManager.setWorldBorder(gameManager.getSurvivalGamesWorld().getWorldBorder());
        gameManager.getWorldBorder().setCenter(gameManager.getSurvivalGamesWorld().getSpawnLocation());
        gameManager.getWorldBorder().setSize(400);
        gameManager.getWorldBorder().setDamageAmount(0.25);
        gameManager.getWorldBorder().setDamageBuffer(3);

        new BukkitRunnable() {
            String formattedTime;

            @Override
            public void run() {
                formattedTime = gameManager.getFormattedTime(timer);

                for (UUID uuid : playerManager.getPlayers()) {
                    Optional.ofNullable(Bukkit.getPlayer(uuid))
                            .ifPresent(player -> gameManager.updateScoreboard(player, nextEvent, formattedTime, playerManager.getKills().get(player.getName())));
                }

                if (timer == 0) {
                    if (nextEvent.equals("Game End")) {
                        this.cancel();
                        gameManager.setGameState(nextState());
                    } else {
                        startEvent(nextEvent);
                    }
                }

                if (playerManager.getAlivePlayers().size() <= 1) {
                    this.cancel();
                    gameManager.setGameState(nextState());
                }
                timer--;
            }
        }.runTaskTimer(survivalGames, 0, 20);
    }

    public void startEvent(String event) {
        switch (event) {
            case "Chest Refill":
                for (Chest chest : checkedChests) {
                    chest.close();
                }
                checkedChests.clear();
                nextEvent = "World Border";
                for (UUID uuid : playerManager.getPlayers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        player.sendTitle(ChatColor.GREEN + "Chests Have Refilled!", "", 20, 100, 20);
                        player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F);
                    }
                }
                timer = 300;
                break;
            case "World Border":
                gameManager.getWorldBorder().setSize(10, TimeUnit.SECONDS, 200);
                for (UUID uuid : playerManager.getPlayers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        player.sendTitle(ChatColor.RED + "World Border Incoming!", "", 20, 100, 20);
                        player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 1.0F, 0.5F);
                    }
                }
                nextEvent = "Game End";
                timer = 300;
                break;
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
        if (e.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) e.getInventory().getHolder();
            if (!checkedChests.contains(chest)) {
                boolean weapon = true;
                boolean rare = true;
                chest.getInventory().clear();
                while (chest.getInventory().isEmpty()) {
                    for (int i = 0; i < chest.getInventory().getSize(); i++) {
                        ChestItem item = new ChestItem(new Random().nextInt(23));
                        if (item.getItemType() == ChestItem.Type.WEAPON && !weapon) continue;
                        if (item.getItemType() == ChestItem.Type.RARE && !rare) continue;
                        if (new Random().nextFloat() < item.getChance()) {
                            item.setAmount(random.nextInt(item.getMaximum() - item.getMinimum() + 1) + item.getMinimum());
                            chest.getInventory().setItem(i, item.getItem());
                            if (item.getItemType() == ChestItem.Type.WEAPON) weapon = false;
                            if (item.getItemType() == ChestItem.Type.RARE) rare = false;
                        }
                    }
                }
                checkedChests.add(chest);
            }
        } else if (e.getInventory() instanceof EnchantingInventory) {
            EnchantingInventory inventory = (EnchantingInventory) e.getInventory();
            ItemStack lapis = new ItemStack(Material.LAPIS_LAZULI);
            lapis.setAmount(64);
            inventory.setItem(1, lapis);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) e.getInventory().getHolder();
            if (chest.getInventory().isEmpty()) {
                chest.open();
            } else {
                chest.close();
            }
        } else if (e.getInventory() instanceof EnchantingInventory) {
            EnchantingInventory inventory = (EnchantingInventory) e.getInventory();
            inventory.clear(1);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() instanceof EnchantingInventory) {
            if (e.getSlot() == 1) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            e.setCancelled(true);
            return;
        }

        if ((e.getEntity() instanceof Player && playerManager.getPlayers().contains(e.getEntity().getUniqueId()))) {
            if (e.getDamage() >= ((Player) e.getEntity()).getHealth()) {
                e.setCancelled(true);
                Player damager = null;
                if (e.getDamager() instanceof Player) {
                    damager = (Player) e.getDamager();
                } else if (((Arrow) e.getDamager()).getShooter() instanceof Player) {
                    damager = (Player) ((Arrow) e.getDamager()).getShooter();
                }
                if (damager != null && playerManager.getAlivePlayers().contains(damager.getUniqueId())) {
                    playerManager.getKills().put(damager.getName(), playerManager.getKills().get(damager.getName()) + 1);
                    for (UUID uuid : playerManager.getPlayers()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            player.sendMessage(gameManager.getChatPrefix() + ChatColor.RED + " " +
                                    e.getEntity().getName() + ChatColor.YELLOW + " was killed by " + ChatColor.RED +
                                    damager.getName() + ChatColor.YELLOW + "!");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && playerManager.getPlayers().contains(e.getEntity().getUniqueId())) {
            Player player = (Player) e.getEntity();
            if (e.getDamage() >= player.getHealth()) {
                e.setCancelled(true);
                player.getWorld().strikeLightningEffect(player.getLocation());
                player.setGameMode(GameMode.SPECTATOR);
                playerManager.getAlivePlayers().remove(player.getUniqueId());
                for (ItemStack itemStack : player.getInventory().getContents()) {
                    if (itemStack != null) {
                        player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                    }
                }
                player.getInventory().clear();

            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (playerManager.getPlayers().contains(e.getPlayer().getUniqueId())) {
            e.setQuitMessage("");
            playerManager.removePlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (playerManager.getPlayers().contains(e.getEntity().getUniqueId())) {
            if (!playerManager.getAlivePlayers().contains((e.getEntity().getUniqueId()))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL) {
            e.setCancelled(true);
        }
    }
}