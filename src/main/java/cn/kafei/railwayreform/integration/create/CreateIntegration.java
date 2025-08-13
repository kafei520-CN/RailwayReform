package cn.kafei.railwayreform.integration.create;

import cn.kafei.railwayreform.common.handler.AutoDriveHandler;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint.SteerDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class CreateIntegration {
    
    // 存储每个列车的速度设置
    private static final Map<UUID, Float> trainSpeedSettings = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> trainDirectionSettings = new ConcurrentHashMap<>();
    
    public static void register() {
        // 注册机械动力特定集成
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            handleCreateTrainMovement();
        }
    }

    public static void handleCreateTrainMovement() {
        ServerLevel level = AutoDriveHandler.getLevel();
        if (level == null) return;

        // 获取所有运行中的列车
        Map<UUID, Boolean> autoDrivingTrains = AutoDriveHandler.getAutoDrivingTrains();
        
        for (Map.Entry<UUID, Boolean> entry : autoDrivingTrains.entrySet()) {
            if (!entry.getValue()) continue;

            Train train = com.simibubi.create.Create.RAILWAYS.sided(level).trains.get(entry.getKey());
            if (train != null) {
                handleAutoDrive(train);
            }
        }
    }

    private static void handleAutoDrive(Train train) {
        // 从存储的设置中获取当前列车的速度和方向
        float targetSpeed = trainSpeedSettings.getOrDefault(train.id, 1.0f);
        int direction = trainDirectionSettings.getOrDefault(train.id, 1);
        
        System.out.println("RailwayReform: AutoDrive - Train: " + train.id + 
                         " Setting speed to: " + targetSpeed + 
                         " Direction: " + direction + 
                         " Current speed: " + train.speed);
        
        // 根据方向调整速度值
        float actualSpeed = targetSpeed;
        if (direction == 2) { // 后退
            actualSpeed = -Math.abs(targetSpeed);
        } else if (direction == 1) { // 前进
            actualSpeed = Math.abs(targetSpeed);
        } else { // 停止
            actualSpeed = 0;
        }
        
        // 激活列车
        train.targetSpeed = Math.abs(actualSpeed);
        train.throttle = actualSpeed != 0 ? 1.0 : 0.0; // 根据实际速度设置油门
        
        // 确保列车不是手动控制模式
        train.manualSteer = SteerDirection.NONE;
        train.currentStation = null; // 清除车站状态，允许列车移动
        
        // 添加燃料以确保列车能够移动
        train.fuelTicks = 1000;
        
        // 强制离开车站状态
        train.leaveStation();
        
        // 确保列车能够前进 - 设置导航为前进模式
        if (train.navigation != null && train.graph != null) {
            // 清除任何现有的导航状态
            train.navigation.cancelNavigation();
            
            // 设置一个虚拟的前进导航状态
            // 通过设置manualTick为true，让列车沿着当前方向前进
            train.manualTick = true;
            
            // 强制启动列车移动 - 直接设置速度
            train.speed = actualSpeed * 0.5f; // 根据方向设置速度
            
            System.out.println("RailwayReform: AutoDrive - Train activated with direction: " + direction + " for train: " + train.id);
        }
        
        System.out.println("RailwayReform: AutoDrive - Train activated: " + train.id);
    }

    private static boolean hasSwitchAhead(Train train) {
        if (train.graph == null || train.carriages.isEmpty()) {
            System.out.println("RailwayReform: No graph or carriages for train " + train.id);
            return false;
        }
        
        com.simibubi.create.content.trains.graph.TrackGraph graph = train.graph;
        
        // 获取列车前导点
        com.simibubi.create.content.trains.entity.TravellingPoint leadingPoint = 
            train.carriages.get(0).getLeadingPoint();
        if (leadingPoint == null) {
            System.out.println("RailwayReform: No leading point for train " + train.id);
            return false;
        }
        
        // 获取当前节点
        com.simibubi.create.content.trains.graph.TrackNode currentNode = leadingPoint.node1;
        if (currentNode == null) {
            System.out.println("RailwayReform: No current node for train " + train.id);
            return false;
        }
        
        // 检查当前节点是否为道岔
        Map<com.simibubi.create.content.trains.graph.TrackNode, com.simibubi.create.content.trains.graph.TrackEdge> connections = 
            graph.getConnectionsFrom(currentNode);
        if (connections != null && connections.size() > 1) {
            System.out.println("RailwayReform: Switch detected at current node for train " + train.id + 
                             " Connections: " + connections.size());
            return true;
        }
        
        // 检查前方节点
        com.simibubi.create.content.trains.graph.TrackNode nextNode = leadingPoint.node2;
        if (nextNode != null) {
            Map<com.simibubi.create.content.trains.graph.TrackNode, com.simibubi.create.content.trains.graph.TrackEdge> nextConnections = 
                graph.getConnectionsFrom(nextNode);
            if (nextConnections != null && nextConnections.size() > 1) {
                System.out.println("RailwayReform: Switch detected at next node for train " + train.id + 
                                 " Connections: " + nextConnections.size());
                return true;
            }
        }
        
        System.out.println("RailwayReform: No switch detected for train " + train.id);
        return false;
    }

    public static void setTrainSpeed(com.simibubi.create.content.trains.entity.CarriageContraptionEntity entity, float speed) {
        setTrainSpeed(entity, speed, 1); // 默认前进方向
    }
    
    public static void setTrainSpeed(com.simibubi.create.content.trains.entity.CarriageContraptionEntity entity, float speed, int direction) {
        if (entity == null) return;
        
        Train train = entity.getCarriage().train;
        if (train == null) {
            System.out.println("RailwayReform: AutoDrive - No train found for entity");
            return;
        }
        
        if (speed == 0 && direction == 0) {
            // 停止自动驾驶，清除设置
            trainSpeedSettings.remove(train.id);
            trainDirectionSettings.remove(train.id);
        } else {
            // 存储速度和方向设置
            trainSpeedSettings.put(train.id, speed);
            trainDirectionSettings.put(train.id, direction);
        }
        
        // 根据方向调整速度值
        float actualSpeed = speed;
        if (direction == 2) { // 后退
            actualSpeed = -Math.abs(speed); // 确保为负值
        } else if (direction == 1) { // 前进
            actualSpeed = Math.abs(speed); // 确保为正值
        } else { // 停止
            actualSpeed = 0;
        }
        
        train.targetSpeed = Math.abs(actualSpeed); // 目标速度始终为正
        train.throttle = actualSpeed != 0 ? 1.0 : 0.0; // 根据实际速度设置油门
        train.fuelTicks = 1000;
        train.currentStation = null;
        train.leaveStation();
        train.manualTick = true;
        
        // 设置实际移动速度
        train.speed = actualSpeed * 0.5f; // 给一个初始速度启动
        
        System.out.println("RailwayReform: AutoDrive - Set train speed to " + actualSpeed + " for train " + train.id + " direction: " + direction);
    }
    
    /**
     * 紧急制动 - 立即停止指定列车
     */
    public static void emergencyBrake(com.simibubi.create.content.trains.entity.CarriageContraptionEntity entity) {
        if (entity == null) return;
        
        Train train = entity.getCarriage().train;
        if (train == null) {
            System.out.println("RailwayReform: Emergency brake - No train found for entity");
            return;
        }
        
        // 清除存储的设置
        trainSpeedSettings.remove(train.id);
        trainDirectionSettings.remove(train.id);
        
        // 立即停止列车
        train.targetSpeed = 0;
        train.throttle = 0;
        train.speed = 0;
        train.fuelTicks = 0;
        train.currentStation = null;
        train.leaveStation();
        train.manualTick = false;
        
        System.out.println("RailwayReform: Emergency brake - Train stopped: " + train.id);
    }
}