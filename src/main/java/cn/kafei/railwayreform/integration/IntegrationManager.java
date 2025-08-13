package cn.kafei.railwayreform.integration;

import cn.kafei.railwayreform.integration.create.CreateIntegration;
import cn.kafei.railwayreform.integration.steamnrails.SteamNRailsIntegration;
import net.minecraftforge.fml.ModList;

public class IntegrationManager {
    public static boolean isCreateLoaded;
    public static boolean isSteamNRailsLoaded;

    public static void registerIntegrations() {
        isCreateLoaded = ModList.get().isLoaded("create");
        isSteamNRailsLoaded = ModList.get().isLoaded("steamnrails");

        if (isCreateLoaded) {
            CreateIntegration.register();
        }

        if (isSteamNRailsLoaded) {
            SteamNRailsIntegration.register();
        }
    }
}