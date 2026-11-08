package com.example.flatplayers;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side commands (never sent to the server):
 *   /flatplayers toggle
 *   /flatplayers distance <blocks>   (0 = everyone flat)
 */
@Mod.EventBusSubscriber(modid = FlatPlayersMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlatCommands {

    @SubscribeEvent
    public static void onRegister(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("flatplayers")
                        .then(Commands.literal("toggle").executes(ctx -> {
                            boolean now = !FlatConfig.ENABLED.get();
                            FlatConfig.ENABLED.set(now);
                            FlatConfig.ENABLED.save();
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Flat Players: " + (now ? "ON" : "OFF")), false);
                            return 1;
                        }))
                        .then(Commands.literal("distance")
                                .then(Commands.argument("blocks", DoubleArgumentType.doubleArg(0, 512))
                                        .executes(ctx -> {
                                            double d = DoubleArgumentType.getDouble(ctx, "blocks");
                                            FlatConfig.FLAT_DISTANCE.set(d);
                                            FlatConfig.FLAT_DISTANCE.save();
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    d == 0 ? "Flat distance: everyone is flat"
                                                            : "Players beyond " + d + " blocks are flat"), false);
                                            return 1;
                                        }))));
    }
}
