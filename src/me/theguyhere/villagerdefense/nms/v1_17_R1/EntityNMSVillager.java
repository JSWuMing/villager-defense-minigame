package me.theguyhere.villagerdefense.nms.v1_17_R1;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

import java.util.Objects;

public class EntityNMSVillager extends EntityVillager {

    public EntityNMSVillager(Location location) {
        super(EntityTypes.aV, ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle());
        super.collides = false;
        super.setPosition(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void tick() {
        // Disable normal ticking
    }

    @Override
    public void inactiveTick() {
        // Disable normal ticking
    }

    @Override
    public void saveData(NBTTagCompound nbtTagCompound) {
        // Prevent saving NBT
    }

    @Override
    public boolean d(NBTTagCompound nbttagcompound) {
        // Prevent saving NBT
        return false;
    }

    @Override
    public boolean e(NBTTagCompound nbttagcompound) {
        // Prevent saving NBT
        return false;
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        // Prevent saving NBT
        return nbttagcompound;
    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        // Prevent loading NBT
    }

    @Override
    public void loadData(NBTTagCompound nbttagcompound) {
        // Prevent loading NBT
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public void setCustomName(IChatBaseComponent ichatbasecomponent) {
        // Lock the custom name
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        // Lock the custom name
    }

    @Override
    public EnumInteractionResult a(EntityHuman human, Vec3D vec3d, EnumHand enumhand) {
        // Prevent stand being equipped
        return EnumInteractionResult.c;
    }

    @Override
    public void setSlot(EnumItemSlot enumitemslot, ItemStack itemstack) {
        // Prevent stand being equipped
    }

    @Override
    public void playSound(SoundEffect soundeffect, float f, float f1) {
        // Remove sounds.
    }

    public CraftNMSVillager getBukkitEntity() {
        return new CraftNMSVillager(super.getWorld().getCraftServer(), this);
    }
}
