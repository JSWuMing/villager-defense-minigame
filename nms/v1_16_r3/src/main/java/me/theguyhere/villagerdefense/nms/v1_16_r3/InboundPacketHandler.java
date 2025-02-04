package me.theguyhere.villagerdefense.nms.v1_16_r3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.theguyhere.villagerdefense.common.CommunicationManager;
import me.theguyhere.villagerdefense.common.Utils;
import me.theguyhere.villagerdefense.nms.common.NMSErrors;
import me.theguyhere.villagerdefense.nms.common.PacketListener;
import net.minecraft.server.v1_16_R3.PacketPlayInUpdateSign;
import net.minecraft.server.v1_16_R3.PacketPlayInUseEntity;
import org.bukkit.entity.Player;

/**
 * Class borrowed from filoghost.
 */
class InboundPacketHandler extends ChannelInboundHandlerAdapter {
    public static final String HANDLER_NAME = "villager_defense_listener";
    private final Player player;
    private final PacketListener packetListener;

    InboundPacketHandler(Player player, PacketListener packetListener) {
        this.player = player;
        this.packetListener = packetListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
        try {
            if (packet instanceof PacketPlayInUseEntity) {
                int entityID = (int) Utils.getFieldValue(packet, "a");

                // Left click
                if (Utils.getFieldValue(packet, "action").toString().equalsIgnoreCase("ATTACK")) {
                    packetListener.onAttack(player, entityID);
                }

                // Main hand right click
                else if (Utils.getFieldValue(packet, "action").toString().equalsIgnoreCase("INTERACT")
                        && Utils.getFieldValue(packet, "d").toString().equalsIgnoreCase("MAIN_HAND")) {
                    packetListener.onInteractMain(player, entityID);
                }
            }

            else if (packet instanceof PacketPlayInUpdateSign) {
                String[] signLines = ((String[]) Utils.getFieldValue(packet, "b"));

                packetListener.onSignUpdate(player, signLines);
            }
        } catch (Exception e) {
            CommunicationManager.debugError(NMSErrors.EXCEPTION_ON_PACKET_READ, 0);
            e.printStackTrace();
        }
        super.channelRead(context, packet);
    }
}
