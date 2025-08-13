package cn.kafei.railwayreform.common.network;

import cn.kafei.railwayreform.common.handler.AutoDriveHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleAutoDrivePacket {
    private final int vehicleId;

    public ToggleAutoDrivePacket(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public static void encode(ToggleAutoDrivePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.vehicleId);
    }

    public static ToggleAutoDrivePacket decode(FriendlyByteBuf buffer) {
        return new ToggleAutoDrivePacket(buffer.readInt());
    }

    public static void handle(ToggleAutoDrivePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            System.out.println("RailwayReform: Received toggle packet from player " + player.getName().getString() + " for vehicle ID: " + packet.vehicleId);
            
            if (player != null) {
                // 获取世界中的实体，不再强制要求玩家骑乘该实体
                Entity vehicle = player.level().getEntity(packet.vehicleId);
                if (vehicle != null) {
                    // 检查玩家是否在合理范围内（10格内）
                    double distance = player.distanceTo(vehicle);
                    if (distance <= 10.0) {
                        System.out.println("RailwayReform: Processing toggle for vehicle " + vehicle.getUUID() + " at distance " + distance);
                        AutoDriveHandler.toggleAutoDrive(vehicle, player);
                    } else {
                        System.out.println("RailwayReform: Vehicle too far - distance: " + distance);
                    }
                } else {
                    System.out.println("RailwayReform: Vehicle not found with ID: " + packet.vehicleId);
                }
            }
        });
    }
}