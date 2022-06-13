package me.theguyhere.villagerdefense.nms.v1_19_r1;

import me.theguyhere.villagerdefense.nms.common.PacketGroup;
import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Abstract packet class for specific NMS version.
 *
 * This abstract class was borrowed from filoghost.
 */
abstract class VersionNMSPacket implements PacketGroup {

    /**
     * Send packet group to player.
     *
     * @param player Recipient.
     */
    @Override
    public void sendTo(Player player) {
        ((CraftPlayer) player).getHandle().b.a(getRawPacket());
    }

    abstract Packet<?> getRawPacket();
}
