package cn.kafei.railwayreform;

import cn.kafei.railwayreform.client.input.KeyBindings;
import cn.kafei.railwayreform.client.input.KeyInputHandler;
import cn.kafei.railwayreform.common.config.ConfigManager;
import cn.kafei.railwayreform.common.handler.AutoDriveHandler;
import cn.kafei.railwayreform.common.network.NetworkManager;
import cn.kafei.railwayreform.integration.IntegrationManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("railwayreform")
public class RailwayReformMod {
    public static final String MOD_ID = "railwayreform";

    public RailwayReformMod() {
        // 初始化配置
        ConfigManager.init();

        // 注册事件总线
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        // 注册模块
        IntegrationManager.registerIntegrations();

        // 注册网络通信
        NetworkManager.registerPackets();

        // 客户端初始化
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(this::onClientSetup);
        });

        // 服务器事件
        forgeEventBus.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        AutoDriveHandler.setLevel(event.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD));
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }
}