package me.theguyhere.villagerdefense.plugin.game.models.items.armor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.theguyhere.villagerdefense.common.ColoredMessage;
import me.theguyhere.villagerdefense.common.CommunicationManager;
import me.theguyhere.villagerdefense.common.Utils;
import me.theguyhere.villagerdefense.plugin.game.models.items.ItemMetaKey;
import me.theguyhere.villagerdefense.plugin.tools.ItemManager;
import me.theguyhere.villagerdefense.plugin.tools.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class Helmet extends VDArmor{
    @NotNull
    public static ItemStack create(HelmetType type) {
        List<String> lores = new ArrayList<>();
        Multimap<Attribute, AttributeModifier> attributes = ArrayListMultimap.create();

        // Set material
        Material mat;
        switch (type) {
            case T1:
            case T2:
                mat = Material.LEATHER_HELMET;
                break;
            case T3:
            case T4:
                mat = Material.CHAINMAIL_HELMET;
                break;
            case T5:
            case T6:
            case T7:
                mat = Material.IRON_HELMET;
                break;
            case T8:
            case T9:
                mat = Material.DIAMOND_HELMET;
                break;
            case T10:
                mat = Material.NETHERITE_HELMET;
                break;
            default:
                mat = Material.GOLDEN_HELMET;
        }

        // Set name
        String name;
        switch (type) {
            case T1:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t1.name),
                        "[T1]"
                );
                break;
            case T2:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t2.name),
                        "[T2]"
                );
                break;
            case T3:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t3.name),
                        "[T3]"
                );
                break;
            case T4:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t4.name),
                        "[T4]"
                );
                break;
            case T5:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t5.name),
                        "[T5]"
                );
                break;
            case T6:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t6.name),
                        "[T6]"
                );
                break;
            case T7:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t7.name),
                        "[T7]"
                );
                break;
            case T8:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t8.name),
                        "[T8]"
                );
                break;
            case T9:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t9.name),
                        "[T9]"
                );
                break;
            case T10:
                name = CommunicationManager.format(
                        new ColoredMessage(LanguageManager.itemLore.helmets.t10.name),
                        "[T10]"
                );
                break;
            default:
                name = "";
        }

        // Set description
        String description;
        switch (type) {
            case T1:
                description = LanguageManager.itemLore.helmets.t1.description;
                break;
            case T2:
                description = LanguageManager.itemLore.helmets.t2.description;
                break;
            case T3:
                description = LanguageManager.itemLore.helmets.t3.description;
                break;
            case T4:
                description = LanguageManager.itemLore.helmets.t4.description;
                break;
            case T5:
                description = LanguageManager.itemLore.helmets.t5.description;
                break;
            case T6:
                description = LanguageManager.itemLore.helmets.t6.description;
                break;
            case T7:
                description = LanguageManager.itemLore.helmets.t7.description;
                break;
            case T8:
                description = LanguageManager.itemLore.helmets.t8.description;
                break;
            case T9:
                description = LanguageManager.itemLore.helmets.t9.description;
                break;
            case T10:
                description = LanguageManager.itemLore.helmets.t10.description;
                break;
            default:
                description = "";
        }
        if (!description.isEmpty())
            lores.addAll(CommunicationManager.formatDescriptionList(
                    ChatColor.GRAY, description, Utils.LORE_CHAR_LIMIT));

        // Add space in lore from name
        lores.add("");

        // Set armor
        int armor;
        switch (type) {
            case T1:
                armor = 2;
                break;
            case T2:
                armor = 3;
                break;
            case T3:
                armor = 5;
                break;
            case T4:
                armor = 6;
                break;
            case T5:
                armor = 8;
                break;
            case T6:
                armor = 9;
                break;
            case T7:
                armor = 10;
                break;
            case T8:
                armor = 12;
                break;
            case T9:
                armor = 14;
                break;
            case T10:
                armor = 16;
                break;
            default:
                armor = 0;
        }
        if (armor > 0)
            lores.add(CommunicationManager.format(ARMOR, new ColoredMessage(ChatColor.AQUA, Integer.toString(armor))));

        // Set toughness
        int toughness;
        switch (type) {
            case T4:
                toughness = 1;
                break;
            case T5:
                toughness = 2;
                break;
            case T6:
                toughness = 3;
                break;
            case T7:
                toughness = 4;
                break;
            case T8:
                toughness = 6;
                break;
            case T9:
                toughness = 7;
                break;
            case T10:
                toughness = 9;
                break;
            default:
                toughness = 0;
        }
        if (toughness > 0)
            lores.add(CommunicationManager.format(TOUGHNESS, new ColoredMessage(ChatColor.DARK_AQUA,
                    toughness + "%")));

        // Set weight
        int weight;
        switch (type) {
            case T1:
            case T2:
                weight = 1;
                break;
            case T3:
            case T4:
                weight = 2;
                break;
            case T5:
            case T6:
            case T7:
                weight = 3;
                break;
            case T8:
            case T9:
                weight = 4;
                break;
            case T10:
                weight = 5;
                break;
            default:
                weight = 0;
        }
        lores.add(CommunicationManager.format(WEIGHT, new ColoredMessage(ChatColor.DARK_PURPLE,
                Integer.toString(weight))));
        attributes.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                new AttributeModifier(ItemMetaKey.DUMMY.name(), 0,
                        AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        attributes.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                new AttributeModifier(ItemMetaKey.DUMMY.name(), weight * .01,
                        AttributeModifier.Operation.ADD_NUMBER));

        // Set durability
        int durability;
        switch (type) {
            case T1:
                durability = 50;
                break;
            case T2:
                durability = 70;
                break;
            case T3:
                durability = 110;
                break;
            case T4:
                durability = 135;
                break;
            case T5:
                durability = 180;
                break;
            case T6:
                durability = 215;
                break;
            case T7:
                durability = 250;
                break;
            case T8:
                durability = 320;
                break;
            case T9:
                durability = 360;
                break;
            case T10:
                durability = 475;
                break;
            default: durability = 0;
        }
        lores.add(CommunicationManager.format(DURABILITY,
                new ColoredMessage(ChatColor.GREEN, Integer.toString(durability)).toString() +
                        new ColoredMessage(ChatColor.WHITE, " / " + durability)));

        // Set price
        int price;
        switch (type) {
            case T1:
                price = 130;
                break;
            case T2:
                price = 165;
                break;
            case T3:
                price = 215;
                break;
            case T4:
                price = 245;
                break;
            case T5:
                price = 310;
                break;
            case T6:
                price = 345;
                break;
            case T7:
                price = 385;
                break;
            case T8:
                price = 465;
                break;
            case T9:
                price = 555;
                break;
            case T10:
                price = 715;
                break;
            default: price = -1;
        }
        if (price >= 0)
            lores.add(CommunicationManager.format("&2" + LanguageManager.messages.gems + ": &a" +
                    price));

        // Create item
        ItemStack item = ItemManager.createItem(mat, name, ItemManager.BUTTON_FLAGS, null, lores, attributes);
        if (durability == 0)
            return ItemManager.makeUnbreakable(item);
        else return item;
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
        return toCheck.getType().toString().contains("HELMET") && lore.stream().anyMatch(line -> line.contains(
                ARMOR.toString().replace("%s", "")));
    }

    public enum HelmetType{
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10
    }
}