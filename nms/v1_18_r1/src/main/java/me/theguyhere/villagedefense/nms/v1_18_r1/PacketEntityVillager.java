package me.theguyhere.villagedefense.nms.v1_18_r1;

import me.theguyhere.villagedefense.nms.common.EntityID;
import me.theguyhere.villagedefense.nms.common.PacketGroup;
import me.theguyhere.villagedefense.nms.common.VillagerPacketEntity;
import org.bukkit.Location;

/**
 * A villager entity constructed out of packets.
 */
class PacketEntityVillager implements VillagerPacketEntity {
    private final EntityID villagerID;

    PacketEntityVillager(EntityID villagerID) {
        this.villagerID = villagerID;
    }

    @Override
    public PacketGroup newDestroyPackets() {
        return new EntityDestroyPacket(villagerID);
    }

    @Override
    public PacketGroup newSpawnPackets(Location location) {
        return PacketGroup.of(
                new SpawnEntityLivingPacket(villagerID, EntityTypeID.VILLAGER, location, location.getPitch()),
                new EntityHeadRotationPacket(villagerID, location.getYaw())
        );
    }
}
