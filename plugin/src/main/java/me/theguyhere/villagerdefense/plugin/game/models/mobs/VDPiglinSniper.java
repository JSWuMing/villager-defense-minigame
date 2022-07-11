package me.theguyhere.villagerdefense.plugin.game.models.mobs;

import me.theguyhere.villagerdefense.plugin.game.models.arenas.Arena;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Piglin;

import java.util.Objects;

public class VDPiglinSniper extends VDMinion {
    public static final String KEY = "pgsn";

    protected VDPiglinSniper(Arena arena, Location location) {
        super(
                arena,
                (Mob) Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.PIGLIN),
                "Piglin Sniper",
                "A long-ranged sniper shooting heavy bolts that both penetrate armor and pierce through targets, " +
                        "aiming for players and especially ranged players.",
                getLevel(arena.getCurrentDifficulty(), 1.5, 4),
                AttackType.PENETRATING
        );
        Piglin piglinSniper = (Piglin) mob;
        piglinSniper.setAdult();
        piglinSniper.setImmuneToZombification(true);
        setHealth(120, 10, level, 2);
        setArmor(5, 2, level, 2);
        setToughness(.1, .05, level, 2);
        setDamage(60, 4, level, 2, .05);
        pierce = 2;
        setSlowAttackSpeed();
        setModerateKnockback();
        setMediumWeight();
        setSlowSpeed();
        targetPriority = TargetPriority.RANGED_PLAYERS;
        setFarTargetRange();
        setArmorEquipment();
        setCrossbow();
        setLoot(45, 1.2, level, .25);
        updateNameTag();
    }
}
