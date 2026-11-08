package com.example.flatplayers;

import net.minecraftforge.common.ForgeConfigSpec;

public class FlatConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.DoubleValue FLAT_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue SHOW_NAMETAG;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        ENABLED = b.comment("Render other players as flat paper sheets.")
                .define("enabled", true);
        FLAT_DISTANCE = b.comment(
                        "Players CLOSER than this many blocks stay normal 3D.",
                        "Players at or beyond this distance become a flat sheet.",
                        "0 = every other player is flat.")
                .defineInRange("flatDistance", 0.0D, 0.0D, 512.0D);
        SHOW_NAMETAG = b.comment("Draw name tags above flat players.")
                .define("showNametag", true);
        SPEC = b.build();
    }
}
