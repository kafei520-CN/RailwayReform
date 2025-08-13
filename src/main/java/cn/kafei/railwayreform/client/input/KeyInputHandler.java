package cn.kafei.railwayreform.client.input;

import cn.kafei.railwayreform.client.gui.TrainSettingsScreen;
import cn.kafei.railwayreform.common.network.NetworkManager;
import cn.kafei.railwayreform.common.network.ToggleAutoDrivePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.List;

public class KeyInputHandler {
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (KeyBindings.TOGGLE_AUTO_DRIVE.consumeClick() && mc.screen == null) {
            System.out.println("RailwayReform: H key pressed, checking for trains...");
            
            // 检查玩家是否骑乘Create模组的列车
            Entity vehicle = mc.player.getVehicle();
            System.out.println("RailwayReform: Player vehicle: " + (vehicle != null ? vehicle.getClass().getName() : "null"));
            
            if (vehicle == null) {
                // 检查玩家是否靠近或骑乘列车实体
                vehicle = findNearestTrainEntity(mc.player);
            }
            
            if (vehicle != null) {
                System.out.println("RailwayReform: Found train entity: " + vehicle.getClass().getName() + " ID: " + vehicle.getId());
                // 打开列车设置GUI
                mc.setScreen(new TrainSettingsScreen(vehicle.getId()));
            } else {
                System.out.println("RailwayReform: Open GUI key pressed but no train entity found nearby");
                // 调试：列出附近的实体
                listNearbyEntities(mc.player);
            }
        }
    }

    private Entity findNearestTrainEntity(net.minecraft.world.entity.player.Player player) {
        // 首先检查玩家是否骑乘任何实体
        if (player.getVehicle() != null) {
            Entity vehicle = player.getVehicle();
            // 检查是否是Create模组的列车实体
            String className = vehicle.getClass().getName();
            if (className.contains("CarriageContraptionEntity") || 
                className.contains("Train") || 
                className.contains("carriage") || 
                className.contains("train")) {
                return vehicle;
            }
        }
        
        // 检查玩家是否正在控制列车（通过射线检测或近距离检测）
        double range = 8.0; // 增加检测范围到8格
        List<Entity> nearbyEntities = player.level().getEntities(player, 
            player.getBoundingBox().inflate(range), 
            entity -> {
                String className = entity.getClass().getName().toLowerCase();
                return className.contains("carriage") || 
                       className.contains("train") ||
                       className.contains("contraption");
            });
        
        // 找到最近的实体
        Entity closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Entity entity : nearbyEntities) {
            double distance = player.distanceTo(entity);
            if (distance < minDistance) {
                minDistance = distance;
                closest = entity;
            }
        }
        
        return closest;
    }

    private void listNearbyEntities(net.minecraft.world.entity.player.Player player) {
        double range = 10.0; // 10格范围内
        List<Entity> nearbyEntities = player.level().getEntities(player, 
            player.getBoundingBox().inflate(range), 
            entity -> true); // 列出所有实体
        
        System.out.println("RailwayReform: Nearby entities within " + range + " blocks:");
        for (Entity entity : nearbyEntities) {
            System.out.println("  - " + entity.getClass().getName() + " (ID: " + entity.getId() + ") at " + 
                String.format("%.1f, %.1f, %.1f", entity.getX(), entity.getY(), entity.getZ()));
        }
        
        if (nearbyEntities.isEmpty()) {
            System.out.println("RailwayReform: No entities found nearby");
        }
    }
}