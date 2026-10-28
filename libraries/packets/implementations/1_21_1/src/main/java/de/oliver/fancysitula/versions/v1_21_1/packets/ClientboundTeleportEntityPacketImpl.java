package de.oliver.fancysitula.versions.v1_21_1.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundTeleportEntityPacket;
import de.oliver.fancysitula.api.utils.AngelConverter;
import de.oliver.fancysitula.versions.v1_21_1.utils.VanillaPlayerAdapter;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;

public class ClientboundTeleportEntityPacketImpl extends FS_ClientboundTeleportEntityPacket {

    public ClientboundTeleportEntityPacketImpl(int entityId, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        super(entityId, x, y, z, yaw, pitch, onGround);
    }

    @Override
    public Object createPacket() {
        // 1.21.1 only has an Entity constructor, so decode the packet from its wire format
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeVarInt(entityId);
            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
            buf.writeByte(AngelConverter.degreesToVanillaByte(yaw));
            buf.writeByte(AngelConverter.degreesToVanillaByte(pitch));
            buf.writeBoolean(onGround);
            return ClientboundTeleportEntityPacket.STREAM_CODEC.decode(buf);
        } finally {
            buf.release();
        }
    }

    @Override
    public void sendPacketTo(FS_RealPlayer player) {
        ClientboundTeleportEntityPacket packet = (ClientboundTeleportEntityPacket) createPacket();

        ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
        vanillaPlayer.connection.send(packet);
    }
}
