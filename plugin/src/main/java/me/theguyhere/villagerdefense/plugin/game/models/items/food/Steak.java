package me.theguyhere.villagerdefense.plugin.game.models.items.food;

import me.theguyhere.villagerdefense.common.ColoredMessage;
import me.theguyhere.villagerdefense.common.CommunicationManager;
import me.theguyhere.villagerdefense.common.Utils;
import me.theguyhere.villagerdefense.plugin.tools.ItemManager;
import me.theguyhere.villagerdefense.plugin.tools.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Steak extends VDFood {
    @NotNull
    public static ItemStack create() {
        return ItemManager.createItem(Material.COOKED_BEEF, null,
                new ColoredMessage(ChatColor.RED, "+160 " + Utils.HP).toString(),
                new ColoredMessage(ChatColor.BLUE, "+6 " + Utils.HUNGER).toString(),
                CommunicationManager.format("&2" + LanguageManager.messages.gems + ": &a550"));
    }

    public static boolean matches(ItemStack toCheck) {
        if (toCheck == null)
            return false;
        ItemMeta meta = toCheck.getItemMeta();
        if (meta == null)
            return false;
        List<String> lore = meta.getLore();
        if (lore == null)
            return false;
        return toCheck.getType() == Material.COOKED_BEEF && lore.stream().anyMatch(line -> line.contains(
                new ColoredMessage(ChatColor.RED, "+160 " + Utils.HP).toString()));
    }
}
