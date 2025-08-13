package cn.kafei.railwayreform.common.network;

import cn.kafei.railwayreform.RailwayReformMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkManager {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(RailwayReformMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registerPackets() {
        registerPacket(ToggleAutoDrivePacket.class,
                ToggleAutoDrivePacket::encode,
                ToggleAutoDrivePacket::decode,
                ToggleAutoDrivePacket::handle);
        registerPacket(AutoDriveSettingsPacket.class,
                AutoDriveSettingsPacket::encode,
                AutoDriveSettingsPacket::decode,
                AutoDriveSettingsPacket::handle);
    }

    private static <T> void registerPacket(Class<T> clazz,
                                           BiConsumer<T, FriendlyByteBuf> encoder,
                                           Function<FriendlyByteBuf, T> decoder,
                                           BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        CHANNEL.registerMessage(packetId++, clazz, encoder, decoder, (packet, context) -> {
            context.get().enqueueWork(() -> handler.accept(packet, context));
            context.get().setPacketHandled(true);
        });
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}