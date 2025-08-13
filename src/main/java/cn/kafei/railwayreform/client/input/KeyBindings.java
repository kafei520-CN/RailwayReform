package cn.kafei.railwayreform.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    // 自动驾驶开关按键
    public static final KeyMapping TOGGLE_AUTO_DRIVE = new KeyMapping(
            "key.railwayreform.toggle_auto_drive",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.railwayreform"
    );

    public static void register() {
        // 注册按键绑定由ClientRegistration处理
    }
}