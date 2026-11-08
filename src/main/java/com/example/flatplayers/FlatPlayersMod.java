package com.example.flatplayers;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;

@Mod(FlatPlayersMod.MODID)
public class FlatPlayersMod {
    public static final String MODID = "flatplayers";

    public FlatPlayersMod() {
        // Client-only mod: tell Forge to accept ANY server (vanilla, or Forge without this mod).
        // The mod registers no network channels, so nothing needs to match on the server.
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remote, isServer) -> true));

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, FlatConfig.SPEC);
    }
}
