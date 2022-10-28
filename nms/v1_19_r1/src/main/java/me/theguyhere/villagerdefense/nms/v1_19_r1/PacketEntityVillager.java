package me.theguyhere.villagerdefense.nms.v1_19_r1;

import me.theguyhere.villagerdefense.nms.common.EntityID;
import me.theguyhere.villagerdefense.nms.common.PacketGroup;
import me.theguyhere.villagerdefense.nms.common.entities.VillagerPacketEntity;
import org.bukkit.Location;

/**
 * A villager entity constructed out of packets.
 */
class PacketEntityVillager implements VillagerPacketEntity {
    private final EntityID villagerID;
    private final String type;

    PacketEntityVillager(EntityID villagerID, String type) {
        this.villagerID = villagerID;
        this.type = type;
    }

    @Override
    public PacketGroup newDestroyPackets() {
        return new EntityDestroyPacket(villagerID);
    }

    @Override
    public PacketGroup newSpawnPackets(Location location) {
        return PacketGroup.of(
                new SpawnEntityPacket(villagerID, EntityTypeID.VILLAGER, location, location.getPitch()),
                new EntityHeadRotationPacket(villagerID, location.getYaw()),
                EntityMetadataPacket.builder(villagerID)
                        .setVillagerType(type)
                        .build()
        );
    }

    @Override
    public int getEntityID() {
        return villagerID.getNumericID();
    }
}