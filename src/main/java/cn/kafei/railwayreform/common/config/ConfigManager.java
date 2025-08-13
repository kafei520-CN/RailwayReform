package cn.kafei.railwayreform.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigManager {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> AUTO_DRIVE_KEY;
    public static final ForgeConfigSpec.DoubleValue MAX_AUTO_SPEED;
    public static final ForgeConfigSpec.IntValue SWITCH_DETECT_RANGE;

    static {
        BUILDER.push("AutoDrive Settings");

        AUTO_DRIVE_KEY = BUILDER
                .comment("Key binding for toggling auto drive")
                .define("autoDriveKey", "key.keyboard.h");

        MAX_AUTO_SPEED = BUILDER
                .comment("Maximum speed in auto drive mode (0.0-2.0)")
                .defineInRange("maxAutoSpeed", 0.8, 0.0, 2.0);

        SWITCH_DETECT_RANGE = BUILDER
                .comment("Switch detection range in blocks")
                .defineInRange("switchDetectRange", 15, 5, 30);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void init() {
        // 配置初始化逻辑
    }
}