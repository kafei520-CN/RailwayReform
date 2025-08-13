package cn.kafei.railwayreform.common.network;

import cn.kafei.railwayreform.common.handler.AutoDriveHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AutoDriveSettingsPacket {
    private final int vehicleId;
    private final int mode;
    private final int direction;
    private final float speed;

    public AutoDriveSettingsPacket(int vehicleId, int mode, int direction, float speed) {
        this.vehicleId = vehicleId;
        this.mode = mode;
        this.direction = direction;
        this.speed = speed;
    }

    public static void encode(AutoDriveSettingsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.vehicleId);
        buffer.writeInt(packet.mode);
        buffer.writeInt(packet.direction);
        buffer.writeFloat(packet.speed);
    }

    public static AutoDriveSettingsPacket decode(FriendlyByteBuf buffer) {
        return new AutoDriveSettingsPacket(
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readFloat()
        );
    }

    public static void handle(AutoDriveSettingsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;

            Entity vehicle = player.level().getEntity(packet.vehicleId);
            if (vehicle != null) {
                // 检查玩家是否在合理范围内
                double distance = player.distanceTo(vehicle);
                if (distance <= 10.0) {
                    System.out.println("RailwayReform: Applying settings to vehicle ID " + packet.vehicleId);
                    System.out.println("  Mode: " + packet.mode);
                    System.out.println("  Direction: " + packet.direction);
                    System.out.println("  Speed: " + packet.speed);
                    
                    // 应用新的设置
                    AutoDriveHandler.applySettings(vehicle, player, packet.mode, packet.direction, packet.speed);
                } else {
                    System.out.println("RailwayReform: Vehicle too far - distance: " + distance);
                }
            } else {
                System.out.println("RailwayReform: Vehicle not found with ID: " + packet.vehicleId);
            }
        });
        context.get().setPacketHandled(true);
    }
}