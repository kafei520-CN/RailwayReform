package cn.kafei.railwayreform.integration.steamnrails;

import cn.kafei.railwayreform.common.config.ConfigManager;
import cn.kafei.railwayreform.common.handler.AutoDriveHandler;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SteamNRailsIntegration {

    public static void register() {
        // 注册汽鸣铁道特定的集成逻辑
    }

    /**
     * 设置汽鸣铁道列车的速度
     */
    public static void setTrainSpeed(Entity vehicle, float speed) {
        setTrainSpeed(vehicle, speed, 1); // 默认前进方向
    }
    
    /**
     * 设置汽鸣铁道列车的速度和方向
     */
    public static void setTrainSpeed(Entity vehicle, float speed, int direction) {
        // 汽鸣铁道使用Create的Train类，支持方向控制
        if (vehicle == null) return;
        
        // 汽鸣铁道列车也使用Create的Train类，所以直接处理
        if (vehicle instanceof com.simibubi.create.content.trains.entity.CarriageContraptionEntity) {
            com.simibubi.create.content.trains.entity.CarriageContraptionEntity carriageEntity = 
                (com.simibubi.create.content.trains.entity.CarriageContraptionEntity) vehicle;
            
            com.simibubi.create.content.trains.entity.Train train = carriageEntity.getCarriage().train;
            if (train == null) return;
            
            // 根据方向调整速度值
            float actualSpeed = speed;
            if (direction == 2) { // 后退
                actualSpeed = -Math.abs(speed);
            } else if (direction == 1) { // 前进
                actualSpeed = Math.abs(speed);
            } else { // 停止
                actualSpeed = 0;
            }
            
            train.targetSpeed = Math.abs(actualSpeed);
            train.throttle = actualSpeed != 0 ? 1.0 : 0.0;
            train.fuelTicks = 1000;
            train.currentStation = null;
            train.leaveStation();
            train.manualTick = true;
            train.speed = actualSpeed * 0.5f;
        }
    }

    /**
     * 处理汽鸣铁道列车的自动驾驶逻辑
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 处理所有启用自动驾驶的汽鸣铁道列车
            AutoDriveHandler.getAutoDrivingTrains().forEach((uuid, enabled) -> {
                Train train = AutoDriveHandler.findTrainById(uuid);
                if (train != null) {
                    handleAutoDrive(train);
                }
            });
        }
    }

    private static void handleAutoDrive(Train train) {
        UUID trainId = train.id;
        if (!AutoDriveHandler.isAutoDriving(trainId)) {
            return; // 未启用自动驾驶
        }

        // 获取当前列车的方向设置（这里简化处理，实际应该从AutoDriveHandler获取）
        // 默认前进方向
        int direction = 1;
        
        if (hasSwitchAhead(train)) {
            // 前方有道岔，减速停止
            train.targetSpeed = 0;
        } else {
            // 无障碍，根据方向设置速度
            // 使用最大速度作为默认行驶速度
            float speed = ConfigManager.MAX_AUTO_SPEED.get().floatValue();
            if (direction == 2) { // 后退
                train.targetSpeed = -speed;
            } else { // 前进
                train.targetSpeed = speed;
            }
        }
    }

    /**
     * 检测前方道岔
     */
    private static boolean hasSwitchAhead(Train train) {
        TrackGraph graph = train.graph;
        if (graph == null) return false;

        // 获取列车前导点
        if (train.carriages.isEmpty()) return false;
        
        TravellingPoint leadingPoint = train.carriages.get(0).getLeadingPoint();
        if (leadingPoint == null) return false;

        // 获取当前节点
        TrackNode currentNode = leadingPoint.node1;
        if (currentNode == null) return false;

        // 检查当前节点是否为道岔
        Map<TrackNode, TrackEdge> connections = graph.getConnectionsFrom(currentNode);
        if (connections != null && connections.size() > 1) {
            return true;
        }

        // 检查前方节点
        TrackNode nextNode = leadingPoint.node2;
        if (nextNode != null) {
            Map<TrackNode, TrackEdge> nextConnections = graph.getConnectionsFrom(nextNode);
            if (nextConnections != null && nextConnections.size() > 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取列车当前位置和方向
     */
    public static String getTrainStatus(Train train) {
        if (train.carriages.isEmpty()) {
            return "列车无车厢";
        }
        
        // 获取列车前导点位置
        TravellingPoint leadingPoint = train.carriages.get(0).getLeadingPoint();
        if (leadingPoint == null || train.graph == null) {
            return "位置未知";
        }
        
        Vec3 position = leadingPoint.getPosition(train.graph);
        String direction = train.speed >= 0 ? "FORWARD" : "BACKWARD";
        
        return String.format("位置: %.1f, %.1f, %.1f | 方向: %s | 速度: %.2f",
                position.x, position.y, position.z,
                direction,
                Math.abs(train.speed));
    }
}