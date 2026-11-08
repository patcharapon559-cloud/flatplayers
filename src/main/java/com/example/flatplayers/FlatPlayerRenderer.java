package com.example.flatplayers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Replaces the 3D model of every OTHER player with one flat sheet of "paper"
 * (the front of their skin laid out as a 2D cut-out) that always faces the camera.
 */
@Mod.EventBusSubscriber(modid = FlatPlayersMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlatPlayerRenderer {

    private static final float PIXEL = 0.9375F / 16F; // vanilla player scale: 32px = 1.8 blocks

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = event.getEntity();

        if (!FlatConfig.ENABLED.get()) return;
        if (player == mc.player || player == mc.getCameraEntity()) return; // never touch yourself
        if (player.isSpectator()) return;
        if (!(player instanceof AbstractClientPlayer clientPlayer)) return;

        double range = FlatConfig.FLAT_DISTANCE.get();
        if (range > 0.0D) {
            double distSqr = mc.gameRenderer.getMainCamera().getPosition()
                    .distanceToSqr(player.getX(), player.getY(), player.getZ());
            if (distSqr < range * range) return; // close enough: keep normal 3D model
        }

        event.setCanceled(true); // skip vanilla 3D model

        if (mc.player != null && player.isInvisibleTo(mc.player)) return;

        PoseStack ps = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int light = event.getPackedLight();

        drawSheet(clientPlayer, ps, buffer, light);
        if (FlatConfig.SHOW_NAMETAG.get() && Minecraft.renderNames()) {
            drawNameTag(mc, player, ps, buffer, light);
        }
    }

    private static void drawSheet(AbstractClientPlayer p, PoseStack ps, MultiBufferSource buffer, int light) {
        Minecraft mc = Minecraft.getInstance();
        boolean slim = "slim".equals(p.getModelName());
        int armW = slim ? 3 : 4;

        ResourceLocation tex = p.getSkinTextureLocation();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));

        ps.pushPose();
        // Rotate around Y only so the sheet stays upright and faces the camera.
        ps.mulPose(Axis.YP.rotationDegrees(-mc.gameRenderer.getMainCamera().getYRot()));
        ps.scale(PIXEL, PIXEL, PIXEL);
        PoseStack.Pose pose = ps.last();

        // ---- base skin layer (r = pixels to the right as seen by the viewer, y = pixels up from feet)
        quad(vc, pose, light, -4, 24, 8, 8, 8, 8, 0F);                 // head
        quad(vc, pose, light, -4, 12, 8, 12, 20, 20, 0F);              // body
        quad(vc, pose, light, -4 - armW, 12, armW, 12, 44, 20, 0F);    // right arm (viewer's left)
        quad(vc, pose, light, 4, 12, armW, 12, 36, 52, 0F);            // left arm  (viewer's right)
        quad(vc, pose, light, -4, 0, 4, 12, 4, 20, 0F);                // right leg
        quad(vc, pose, light, 0, 0, 4, 12, 20, 52, 0F);                // left leg

        // ---- outer (second) skin layer, nudged toward the viewer
        float z = -0.1F;
        if (p.isModelPartShown(PlayerModelPart.HAT))
            quad(vc, pose, light, -4, 24, 8, 8, 40, 8, z);
        if (p.isModelPartShown(PlayerModelPart.JACKET))
            quad(vc, pose, light, -4, 12, 8, 12, 20, 36, z);
        if (p.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE))
            quad(vc, pose, light, -4 - armW, 12, armW, 12, 44, 36, z);
        if (p.isModelPartShown(PlayerModelPart.LEFT_SLEEVE))
            quad(vc, pose, light, 4, 12, armW, 12, 52, 52, z);
        if (p.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG))
            quad(vc, pose, light, -4, 0, 4, 12, 4, 36, z);
        if (p.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG))
            quad(vc, pose, light, 0, 0, 4, 12, 4, 52, z);

        ps.popPose();
    }

    /**
     * One textured rectangle in the sheet plane.
     * r0,y0 = lower-left corner as seen by the viewer; w,h = size in skin pixels;
     * u,v = top-left of the matching region in the 64x64 skin texture.
     * The viewer looks along +Z from -Z, so the viewer's right is world -X.
     */
    private static void quad(VertexConsumer vc, PoseStack.Pose pose, int light,
                             int r0, int y0, int w, int h, int u, int v, float z) {
        float x0 = -r0;
        float x1 = -(r0 + w);
        float yb = y0;
        float yt = y0 + h;
        float u0 = u / 64F, u1 = (u + w) / 64F;
        float v0 = v / 64F, v1 = (v + h) / 64F;

        vertex(vc, pose, light, x0, yt, z, u0, v0);
        vertex(vc, pose, light, x0, yb, z, u0, v1);
        vertex(vc, pose, light, x1, yb, z, u1, v1);
        vertex(vc, pose, light, x1, yt, z, u1, v0);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, int light,
                               float x, float y, float z, float u, float v) {
        vc.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), 0F, 0F, -1F)
                .endVertex();
    }

    /** Same logic as vanilla EntityRenderer#renderNameTag (which is protected). */
    private static void drawNameTag(Minecraft mc, Player player, PoseStack ps,
                                    MultiBufferSource buffer, int light) {
        if (mc.getEntityRenderDispatcher().distanceToSqr(player) > 4096.0D) return;

        Component name = player.getDisplayName();
        boolean seeThrough = !player.isDiscrete();
        float height = player.getBbHeight() + 0.5F;

        ps.pushPose();
        ps.translate(0.0F, height, 0.0F);
        ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        ps.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = ps.last().pose();
        int background = (int) (mc.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        Font font = mc.font;
        float x = -font.width(name) / 2.0F;

        font.drawInBatch(name, x, 0, 0x20FFFFFF, false, matrix, buffer,
                seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, background, light);
        if (seeThrough) {
            font.drawInBatch(name, x, 0, -1, false, matrix, buffer,
                    Font.DisplayMode.NORMAL, 0, light);
        }
        ps.popPose();
    }
}
