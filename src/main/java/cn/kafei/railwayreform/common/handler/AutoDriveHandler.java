package cn.kafei.railwayreform.common.handler;

import cn.kafei.railwayreform.common.config.ConfigManager;
import cn.kafei.railwayreform.integration.create.CreateIntegration;
import cn.kafei.railwayreform.integration.steamnrails.SteamNRailsIntegration;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "railwayreform")
public class AutoDriveHandler {
    private static final Map<UUID, Boolean> autoDrivingTrains = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> autoDriveInitiator = new ConcurrentHashMap<>();
    private static ServerLevel serverLevel;

    /**
     * 切换列车的自动驾驶状态
     */
    public static void toggleAutoDrive(Entity vehicle, Player player) {
        if (vehicle == null) {
            System.out.println("RailwayReform: toggleAutoDrive called with null vehicle");
            return;
        }

        // 检查是否为Create模组的列车实体
        if (!(vehicle instanceof com.simibubi.create.content.trains.entity.CarriageContraptionEntity)) {
            System.out.println("RailwayReform: Vehicle is not a CarriageContraptionEntity: " + vehicle.getClass().getSimpleName());
            return;
        }

        com.simibubi.create.content.trains.entity.CarriageContraptionEntity carriageEntity = 
            (com.simibubi.create.content.trains.entity.CarriageContraptionEntity) vehicle;
        
        // 获取实际的Train对象
        com.simibubi.create.content.trains.entity.Train train = carriageEntity.getCarriage().train;
        if (train == null) {
            System.out.println("RailwayReform: Could not get train from carriage entity");
            return;
        }

        UUID trainId = train.id;
        boolean newState = !autoDrivingTrains.getOrDefault(trainId, false);
        autoDrivingTrains.put(trainId, newState);
        
        if (newState) {
            // 记录发起自动驾驶的玩家
            autoDriveInitiator.put(trainId, player.getUUID());
        } else {
            // 清除发起者记录
            autoDriveInitiator.remove(trainId);
        }

        System.out.println("RailwayReform: Toggling auto drive for train " + trainId + " to " + newState);

        if (newState) {
            // 启用自动驾驶，默认前进方向
            CreateIntegration.setTrainSpeed(carriageEntity, 1.0f, 1);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("自动驾驶已开启"), true);
        } else {
            // 关闭自动驾驶，玩家接管
            autoDriveInitiator.remove(trainId);
            CreateIntegration.setTrainSpeed(carriageEntity, 0f, 0);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("自动驾驶已关闭，手动驾驶模式"), true);
        }
    }

    /**
     * 获取所有启用自动驾驶的列车
     */
    public static Map<UUID, Boolean> getAutoDrivingTrains() {
        return autoDrivingTrains;
    }

    /**
     * 设置服务器等级
     */
    public static void setLevel(ServerLevel level) {
        serverLevel = level;
    }

    /**
     * 获取服务器等级
     */
    public static ServerLevel getLevel() {
        return serverLevel;
    }

    /**
     * 检查列车是否启用自动驾驶
     */
    public static boolean isAutoDriving(UUID trainId) {
        return autoDrivingTrains.getOrDefault(trainId, false);
    }

    /**
     * 从自动驾驶列表中移除列车
     */
    public static void removeTrain(UUID trainId) {
        autoDrivingTrains.remove(trainId);
    }

    /**
     * 根据UUID查找列车
     */
    public static Train findTrainById(UUID trainId) {
        ServerLevel level = getLevel();
        if (level == null) return null;
        
        return com.simibubi.create.Create.RAILWAYS.sided(level).trains.get(trainId);
    }

    /**
     * 应用详细的自动驾驶设置
     */
    public static void applySettings(Entity vehicle, Player player, int mode, int direction, float speed) {
        if (vehicle == null) {
            System.out.println("RailwayReform: applySettings called with null vehicle");
            return;
        }

        // 检查是否为Create模组的列车实体
        if (!(vehicle instanceof com.simibubi.create.content.trains.entity.CarriageContraptionEntity)) {
            System.out.println("RailwayReform: Vehicle is not a CarriageContraptionEntity: " + vehicle.getClass().getSimpleName());
            return;
        }

        com.simibubi.create.content.trains.entity.CarriageContraptionEntity carriageEntity = 
            (com.simibubi.create.content.trains.entity.CarriageContraptionEntity) vehicle;
        
        // 获取实际的Train对象
        com.simibubi.create.content.trains.entity.Train train = carriageEntity.getCarriage().train;
        if (train == null) {
            System.out.println("RailwayReform: Could not get train from carriage entity");
            return;
        }

        UUID trainId = train.id;
        
        // 检查是否为紧急制动指令（模式0，方向0，速度0）
        if (mode == 0 && direction == 0 && speed == 0f) {
            emergencyBrake(vehicle, player);
            return;
        }
        
        // 处理不同的驾驶模式
        switch (mode) {
            case 0: // 手动驾驶
                 autoDrivingTrains.remove(trainId);
                 autoDriveInitiator.remove(trainId);
                 CreateIntegration.setTrainSpeed(carriageEntity, 0f);
                 player.displayClientMessage(
                         net.minecraft.network.chat.Component.literal("已切换到手动驾驶模式"), true);
                 break;
                
            case 1: // 全自动驾驶
                 autoDrivingTrains.put(trainId, true);
                 autoDriveInitiator.put(trainId, player.getUUID());
                 CreateIntegration.setTrainSpeed(carriageEntity, 1.0f, direction);
                 player.displayClientMessage(
                         net.minecraft.network.chat.Component.literal("全自动驾驶已开启 - 速度: 1.0"), true);
                 break;
                 
             case 2: // 半自动驾驶
                 autoDrivingTrains.put(trainId, true);
                 autoDriveInitiator.put(trainId, player.getUUID());
                 // 半自动驾驶直接使用滑动条设定的值，不限制最大值
                 CreateIntegration.setTrainSpeed(carriageEntity, speed, direction);
                 
                 // 根据方向设置
                 String directionText = getDirectionText(direction);
                 player.displayClientMessage(
                         net.minecraft.network.chat.Component.literal("半自动驾驶已开启 - " + directionText + " - 速度: " + speed), true);
                 break;
        }
        
        System.out.println("RailwayReform: Applied settings to train " + trainId + 
                " - Mode: " + mode + " - Direction: " + direction + " - Speed: " + speed);
    }
    
    private static String getDirectionText(int direction) {
        switch (direction) {
            case 0: return "停止";
            case 1: return "前进";
            case 2: return "后退";
            case 3: return "自动";
            default: return "未知";
        }
    }

    /**
     * 紧急制动 - 立即停止列车并清除自动驾驶状态
     */
    public static void emergencyBrake(Entity vehicle) {
        emergencyBrake(vehicle, null);
    }
    
    public static void emergencyBrake(Entity vehicle, Player player) {
        if (vehicle == null) {
            System.out.println("RailwayReform: emergencyBrake called with null vehicle");
            return;
        }

        // 检查是否为Create模组的列车实体
        if (!(vehicle instanceof com.simibubi.create.content.trains.entity.CarriageContraptionEntity)) {
            System.out.println("RailwayReform: Vehicle is not a CarriageContraptionEntity: " + vehicle.getClass().getSimpleName());
            return;
        }

        com.simibubi.create.content.trains.entity.CarriageContraptionEntity carriageEntity = 
            (com.simibubi.create.content.trains.entity.CarriageContraptionEntity) vehicle;
        
        // 获取实际的Train对象
        com.simibubi.create.content.trains.entity.Train train = carriageEntity.getCarriage().train;
        if (train == null) {
            System.out.println("RailwayReform: Could not get train from carriage entity");
            return;
        }

        UUID trainId = train.id;
        
        // 从自动驾驶列表中移除列车
        autoDrivingTrains.remove(trainId);
        autoDriveInitiator.remove(trainId);
        
        // 调用CreateIntegration的紧急制动方法
        CreateIntegration.emergencyBrake(carriageEntity);
        
        // 向玩家显示紧急制动消息
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("紧急制动已启动！列车已停止"), true);
        
        System.out.println("RailwayReform: Emergency brake activated for train " + trainId);
    }

    /**
     * 处理Create模组的列车移动逻辑
     */
    public static void handleCreateTrainMovement() {
        // 在CreateIntegration中实现具体逻辑
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();
        
        // 检查是否有该玩家发起的自动驾驶
        for (Map.Entry<UUID, UUID> entry : autoDriveInitiator.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                UUID trainUUID = entry.getKey();
                
                // 找到对应的列车实体
                if (event.getEntity().level() instanceof ServerLevel serverLevel) {
                    serverLevel.getEntities().getAll().forEach(entity -> {
                        if (entity instanceof com.simibubi.create.content.trains.entity.CarriageContraptionEntity contraption && 
                            entity.getUUID().equals(trainUUID)) {
                            // 触发紧急制动
                            emergencyBrake(contraption);
                        }
                    });
                }
            }
        }
    }
}