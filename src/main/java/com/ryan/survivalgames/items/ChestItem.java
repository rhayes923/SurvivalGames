package com.ryan.survivalgames.items;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ChestItem extends ItemStack {

    ItemStack item;
    Type type;
    float chance;
    int minimum;
    int maximum;

    public enum Type {
        WEAPON, ARMOR, FOOD, MATERIAL, RARE
    }

    public ChestItem(int num) {
        switch (num) {
            case 0:
                item = new ItemStack(Material.STONE_SWORD);
                type = Type.WEAPON;
                chance = 0.05F;
                minimum = 1;
                maximum = 1;
                break;
            case 1:
                item = new ItemStack(Material.WOODEN_SWORD);
                type = Type.WEAPON;
                chance = 0.15F;
                minimum = 1;
                maximum = 1;
                break;
            case 2:
                item = new ItemStack(Material.COOKED_BEEF);
                type = Type.FOOD;
                chance = 0.05F;
                minimum = 1;
                maximum = 3;
                break;
            case 3:
                item = new ItemStack(Material.GOLDEN_APPLE);
                type = Type.RARE;
                chance = 0.01F;
                minimum = 1;
                maximum = 1;
                break;
            case 4:
                item = new ItemStack(Material.CHAINMAIL_HELMET);
                type = Type.ARMOR;
                chance = 0.020F;
                minimum = 1;
                maximum = 1;
                break;
            case 5:
                item = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                type = Type.ARMOR;
                chance = 0.010F;
                minimum = 1;
                maximum = 1;
                break;
            case 6:
                item = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                type = Type.ARMOR;
                chance = 0.015F;
                minimum = 1;
                maximum = 1;
                break;
            case 7:
                item = new ItemStack(Material.CHAINMAIL_BOOTS);
                type = Type.ARMOR;
                chance = 0.025F;
                minimum = 1;
                maximum = 1;
                break;
            case 8:
                item = new ItemStack(Material.LEATHER_HELMET);
                type = Type.ARMOR;
                chance = 0.020F;
                minimum = 1;
                maximum = 1;
                break;
            case 9:
                item = new ItemStack(Material.LEATHER_CHESTPLATE);
                type = Type.ARMOR;
                chance = 0.010F;
                minimum = 1;
                maximum = 1;
                break;
            case 10:
                item = new ItemStack(Material.LEATHER_LEGGINGS);
                type = Type.ARMOR;
                chance = 0.015F;
                minimum = 1;
                maximum = 1;
                break;
            case 11:
                item = new ItemStack(Material.LEATHER_BOOTS);
                type = Type.ARMOR;
                chance = 0.025F;
                minimum = 1;
                maximum = 1;
                break;
            case 12:
                item = new ItemStack(Material.STICK);
                type = Type.MATERIAL;
                chance = 0.05F;
                minimum = 1;
                maximum = 1;
                break;
            case 13:
                item = new ItemStack(Material.IRON_INGOT);
                type = Type.MATERIAL;
                chance = 0.05F;
                minimum = 1;
                maximum = 1;
                break;
            case 14:
                item = new ItemStack(Material.DIAMOND);
                type = Type.RARE;
                chance = 0.01F;
                minimum = 1;
                maximum = 1;
                break;
            case 15:
                item = new ItemStack(Material.COOKED_CHICKEN);
                type = Type.FOOD;
                chance = 0.05F;
                minimum = 1;
                maximum = 3;
                break;
            case 16:
                item = new ItemStack(Material.COOKED_PORKCHOP);
                type = Type.FOOD;
                chance = 0.05F;
                minimum = 1;
                maximum = 3;
                break;
            case 17:
                item = new ItemStack(Material.GOLD_INGOT);
                type = Type.MATERIAL;
                chance = 0.10F;
                minimum = 1;
                maximum = 1;
                break;
            case 18:
                item = new ItemStack(Material.APPLE);
                type = Type.RARE;
                chance = 0.025F;
                minimum = 1;
                maximum = 1;
                break;
            case 19:
                ItemStack potion = new ItemStack(Material.POTION);
                PotionMeta meta = (PotionMeta) potion.getItemMeta();
                if (meta != null) {
                    meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1), true);
                    meta.setColor(Color.fromRGB(126, 178, 202));
                    meta.setDisplayName(ChatColor.RESET + "Potion of Swiftness");
                    potion.setItemMeta(meta);
                }
                item = potion;
                type = Type.RARE;
                chance = 0.015F;
                minimum = 1;
                maximum = 1;
                break;
            case 20:
                item = new ItemStack(Material.BOW);
                type = Type.WEAPON;
                chance = 0.05F;
                minimum = 1;
                maximum = 1;
                break;
            case 21:
                item = new ItemStack(Material.ARROW);
                type = Type.MATERIAL;
                chance = 0.15F;
                minimum = 1;
                maximum = 2;
                break;
            case 22:
                item = new ItemStack(Material.EXPERIENCE_BOTTLE);
                type = Type.RARE;
                chance = 0.01F;
                minimum = 1;
                maximum = 1;
        }
    }

    public ItemStack getItem() {
        return item;
    }

    public void setAmount(int num) {
        item.setAmount(num);
    }

    public Type getItemType() {
        return type;
    }

    public float getChance() {
        return chance;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }
}
