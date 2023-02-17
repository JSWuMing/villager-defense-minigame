package me.theguyhere.villagerdefense.plugin.game.models.mobs.pets;

import me.theguyhere.villagerdefense.plugin.Main;
import me.theguyhere.villagerdefense.plugin.game.models.Challenge;
import me.theguyhere.villagerdefense.plugin.game.models.arenas.Arena;
import me.theguyhere.villagerdefense.plugin.game.models.mobs.AttackType;
import me.theguyhere.villagerdefense.plugin.game.models.mobs.Team;
import me.theguyhere.villagerdefense.plugin.game.models.mobs.VDMob;
import me.theguyhere.villagerdefense.plugin.game.models.players.VDPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

public abstract class VDPet extends VDMob {
    private final int slots;
    protected final Material buttonMat;
    protected final VDPlayer owner;

    protected VDPet(Arena arena, Tameable pet, String name, String lore, AttackType attackType, int slots,
                    Material buttonMat, VDPlayer owner) {
        super(lore, attackType);
        mob = pet;
        this.owner = owner;
        pet.setOwner(owner.getPlayer());
        id = pet.getUniqueId();
        pet.setMetadata(TEAM, Team.VILLAGER.getValue());
        pet.setMetadata(VD, new FixedMetadataValue(Main.plugin, arena.getId()));
        gameID = arena.getGameID();
        wave = arena.getCurrentWave();
        this.name = name;
        this.slots = slots;
        this.buttonMat = buttonMat;
        pet.setRemoveWhenFarAway(false);
        pet.setHealth(2);
        pet.setCustomNameVisible(true);
    }

    public int getLevel() {
        return level;
    }

    public int getSlots() {
        return slots;
    }

    public String getName() {
        return mob.getCustomName();
    }

    public abstract void incrementLevel();

    public abstract VDPet respawn(Arena arena, Location location);

    public abstract ItemStack createDisplayButton();

    public abstract ItemStack createUpgradeButton();

    public VDPlayer getOwner() {
        return owner;
    }

    public void heal() {
        // Check if still alive
        if (mob.isDead())
            return;

        // Natural heal
        if (!owner.getChallenges().contains(Challenge.uhc())) {
            int hunger = owner.getPlayer().getFoodLevel();
            if (hunger >= 20)
                changeCurrentHealth(6);
            else if (hunger >= 16)
                changeCurrentHealth(5);
            else if (hunger >= 10)
                changeCurrentHealth(3);
            else if (hunger >= 4)
                changeCurrentHealth(2);
            else if (hunger > 0)
                changeCurrentHealth(1);
        }

        // Regeneration
        mob.getActivePotionEffects().forEach(potionEffect -> {
            if (PotionEffectType.REGENERATION.equals(potionEffect.getType()))
                changeCurrentHealth(5 * (1 + potionEffect.getAmplifier()));
        });

        updateNameTag();
    }

    public void heal(int health) {
        // Check if still alive
        if (mob.isDead())
            return;

        // Heal and update
        changeCurrentHealth(health);
        updateNameTag();
    }

    public void kill() {
        // Check if still alive
        if (mob.isDead())
            return;

        // Kill
        takeDamage(currentHealth, AttackType.DIRECT, null, owner.getArena());
        updateNameTag();
    }

    @Override
    protected void updateNameTag() {
        super.updateNameTag(ChatColor.GREEN);
    }
}
