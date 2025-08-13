package cn.kafei.railwayreform.client.gui;

import cn.kafei.railwayreform.common.network.NetworkManager;
import cn.kafei.railwayreform.common.network.AutoDriveSettingsPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TrainSettingsScreen extends Screen {
    
    private final int entityId;
    
    // 驾驶模式选项
    private int selectedMode = 1; // 默认自动驾驶
    private int selectedDirection = 0; // 默认前进
    private float speedLimit = 0.8f; // 默认速度限制
    
    // 保存的设置
    private static int lastSelectedMode = 1;
    private static int lastSelectedDirection = 0;
    private static float lastSpeedLimit = 0.8f;
    
    private Button modeButton;
    private Button directionButton;
    private AbstractSliderButton speedSlider;
    private Button confirmButton;
    private Button cancelButton;
    private Button emergencyBrakeButton;
    
    public TrainSettingsScreen(int entityId) {
        super(Component.literal("列车控制设置"));
        this.entityId = entityId;
        // 加载上次保存的设置
        this.selectedMode = lastSelectedMode;
        this.selectedDirection = lastSelectedDirection;
        this.speedLimit = lastSpeedLimit;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        // 标题背景
        int titleWidth = 220;
        int titleHeight = 220; // 增加高度以适应紧急制动按钮
        int startX = centerX - titleWidth / 2;
        int startY = centerY - titleHeight / 2;
        
        // 驾驶模式按钮
        modeButton = Button.builder(Component.literal("驾驶模式: " + getModeText(selectedMode)), 
                button -> {
                    selectedMode = (selectedMode + 1) % 3; // 现在支持3种模式
                    modeButton.setMessage(Component.literal("驾驶模式: " + getModeText(selectedMode)));
                    updateButtonStates(); // 更新按钮状态
                })
            .pos(startX + 20, startY + 30)
            .size(180, 25)
            .build();
        this.addRenderableWidget(modeButton);
        
        // 行驶方向按钮
        directionButton = Button.builder(Component.literal("行驶方向: " + getDirectionText(selectedDirection)), 
                button -> {
                    selectedDirection = (selectedDirection + 1) % 4; // 现在支持4种方向
                    directionButton.setMessage(Component.literal("行驶方向: " + getDirectionText(selectedDirection)));
                })
            .pos(startX + 20, startY + 65)
            .size(180, 25)
            .build();
        this.addRenderableWidget(directionButton);
        
        // 速度限制滑动条
        speedSlider = new AbstractSliderButton(
            startX + 20, startY + 100, 180, 20, 
            Component.literal("速度限制: "), 
            (double) speedLimit / 2.0 // 归一化到0-1范围
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("速度限制: " + String.format("%.1f", this.value * 2.0)));
            }

            @Override
            protected void applyValue() {
                speedLimit = (float) (this.value * 2.0);
            }
        };
        this.addRenderableWidget(speedSlider);
        
        // 确认按钮
        confirmButton = Button.builder(Component.literal("确认应用"), 
                button -> {
                    // 保存当前设置
                    lastSelectedMode = selectedMode;
                    lastSelectedDirection = selectedDirection;
                    lastSpeedLimit = speedLimit;
                    
                    // 发送到服务器
                    NetworkManager.sendToServer(new AutoDriveSettingsPacket(
                        entityId, 
                        selectedMode, 
                        selectedDirection, 
                        speedLimit
                    ));
                    this.onClose();
                })
            .pos(startX + 20, startY + 135)
            .size(85, 25)
            .build();
        this.addRenderableWidget(confirmButton);
        
        // 取消按钮
        cancelButton = Button.builder(Component.literal("取消"), 
                button -> this.onClose())
            .pos(startX + 115, startY + 135)
            .size(85, 25)
            .build();
        this.addRenderableWidget(cancelButton);
        
        // 紧急制动按钮
        emergencyBrakeButton = Button.builder(Component.literal("紧急制动"), 
                button -> {
                    // 发送紧急制动指令到服务器
                    NetworkManager.sendToServer(new AutoDriveSettingsPacket(
                        entityId, 
                        0, // 手动模式
                        0, // 停止方向
                        0f // 速度为0
                    ));
                    this.onClose();
                })
            .pos(startX + 20, startY + 165)
            .size(180, 25)
            .build();
        this.addRenderableWidget(emergencyBrakeButton);
        
        // 初始化按钮状态
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        // 根据模式更新按钮状态
        switch (selectedMode) {
            case 0: // 手动驾驶
                directionButton.active = false;
                speedSlider.active = false;
                break;
                
            case 1: // 自动驾驶
                directionButton.active = true;
                speedSlider.active = false; // 全自动模式锁定限速
                break;
                
            case 2: // 半自动驾驶
                directionButton.active = true;
                speedSlider.active = true;
                break;
        }
    }
    
    private String getModeText(int mode) {
        switch (mode) {
            case 0: return "手动驾驶";
            case 1: return "自动驾驶";
            case 2: return "半自动驾驶";
            default: return "未知";
        }
    }
    
    private String getDirectionText(int direction) {
        switch (direction) {
            case 0: return "停止";
            case 1: return "前进";
            case 2: return "后退";
            case 3: return "自动";
            default: return "未知";
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染半透明背景
        this.renderBackground(graphics);
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        // 绘制GUI背景
        int guiWidth = 220;
        int guiHeight = 220; // 增加高度以适应紧急制动按钮
        int startX = centerX - guiWidth / 2;
        int startY = centerY - guiHeight / 2;
        
        // 绘制背景
        graphics.fill(startX, startY, startX + guiWidth, startY + guiHeight, 0x90000000);
        
        // 绘制边框
        graphics.fill(startX, startY, startX + guiWidth, startY + 1, 0xFFFFFFFF);
        graphics.fill(startX, startY, startX + 1, startY + guiHeight, 0xFFFFFFFF);
        graphics.fill(startX + guiWidth - 1, startY, startX + guiWidth, startY + guiHeight, 0xFFFFFFFF);
        graphics.fill(startX, startY + guiHeight - 1, startX + guiWidth, startY + guiHeight, 0xFFFFFFFF);
        
        // 渲染标题
        graphics.drawCenteredString(this.font, this.title, centerX, startY + 8, 0xFFFFA500);
        
        // 渲染说明文字
        graphics.drawCenteredString(this.font, Component.literal("选择驾驶模式、方向和速度"), centerX, startY + 20, 0xFFAAAAAA);
        
        // 显示当前模式提示
        if (selectedMode == 0) {
            graphics.drawCenteredString(this.font, Component.literal("手动模式：其他选项已锁定"), centerX, startY + 190, 0xFFFF6666);
        } else if (selectedMode == 1) {
            graphics.drawCenteredString(this.font, Component.literal("全自动模式：限速已锁定为最大值"), centerX, startY + 190, 0xFFFF6666);
        }
        
        super.render(graphics, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}