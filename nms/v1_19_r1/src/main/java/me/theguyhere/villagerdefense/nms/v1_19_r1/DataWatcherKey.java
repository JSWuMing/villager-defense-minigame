package me.theguyhere.villagerdefense.nms.v1_19_r1;

import io.netty.handler.codec.EncoderException;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.network.syncher.DataWatcherSerializer;
import net.minecraft.world.entity.npc.VillagerData;

import java.util.Optional;

/**
 * A class to simplify interactions with serializing and managing DataWatchers.
 *
 * This class was borrowed from filoghost to learn about using DataWatchers.
 * @param <T>
 */
class DataWatcherKey<T> {
    private static final DataWatcherSerializer<Byte> BYTE_SERIALIZER = DataWatcherRegistry.a;
    private static final DataWatcherSerializer<Boolean> BOOLEAN_SERIALIZER = DataWatcherRegistry.i;
    private static final DataWatcherSerializer<Optional<IChatBaseComponent>> OPTIONAL_CHAT_COMPONENT_SERIALIZER =
            DataWatcherRegistry.f;
    private static final DataWatcherSerializer<VillagerData> VILLAGER_DATA_SERIALIZER = DataWatcherRegistry.r;

    static final DataWatcherKey<Byte> ENTITY_STATUS = new DataWatcherKey<>(0, BYTE_SERIALIZER);
    static final DataWatcherKey<Optional<IChatBaseComponent>> CUSTOM_NAME =
            new DataWatcherKey<>(2, OPTIONAL_CHAT_COMPONENT_SERIALIZER);
    static final DataWatcherKey<Boolean> CUSTOM_NAME_VISIBILITY = new DataWatcherKey<>(3, BOOLEAN_SERIALIZER);
    static final DataWatcherKey<Byte> ARMOR_STAND_STATUS = new DataWatcherKey<>(15, BYTE_SERIALIZER);
    static final DataWatcherKey<VillagerData> VILLAGER_DATA = new DataWatcherKey<>(18, VILLAGER_DATA_SERIALIZER);

    private final int index;
    private final DataWatcherSerializer<T> serializer;
    private final int serializerTypeID;

    private DataWatcherKey(int index, DataWatcherSerializer<T> serializer) {
        this.index = index;
        this.serializer = serializer;
        this.serializerTypeID = DataWatcherRegistry.b(serializer);
        if (serializerTypeID < 0) {
            throw new EncoderException("Could not find serializer ID of " + serializer);
        }
    }

    int getIndex() {
        return index;
    }

    DataWatcherSerializer<T> getSerializer() {
        return serializer;
    }

    int getSerializerTypeID() {
        return serializerTypeID;
    }
}