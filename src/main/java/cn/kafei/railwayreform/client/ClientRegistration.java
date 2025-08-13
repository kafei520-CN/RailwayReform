package cn.kafei.railwayreform.client;

import cn.kafei.railwayreform.RailwayReformMod;
import cn.kafei.railwayreform.client.input.KeyBindings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RailwayReformMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        // 注册所有按键绑定
        event.register(KeyBindings.TOGGLE_AUTO_DRIVE);

    }
}