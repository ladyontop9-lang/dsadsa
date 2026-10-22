package dev.avatrix;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Draws a box around the entity under the crosshair.
 * The crosshair target is already blocked by walls, and the line layer is
 * depth-tested, so the outline is never visible through blocks.
 */
public final class TargetEsp {
    private TargetEsp() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(TargetEsp::render);
    }

    private static void render(WorldRenderContext ctx) {
        if (!State.targetEsp) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc.targetedEntity instanceof LivingEntity e) || e == mc.player) return;

        MatrixStack matrices = ctx.matrixStack();
        VertexConsumerProvider consumers = ctx.consumers();
        if (matrices == null || consumers == null) return;

        float delta = ctx.tickCounter().getTickDelta(true);
        Vec3d cam = ctx.camera().getPos();
        Vec3d shift = e.getLerpedPos(delta).subtract(e.getPos());
        Box box = e.getBoundingBox().offset(shift).offset(-cam.x, -cam.y, -cam.z);

        float r = (State.espColor >> 16 & 255) / 255f;
        float g = (State.espColor >> 8 & 255) / 255f;
        float b = (State.espColor & 255) / 255f;

        VertexRendering.drawBox(matrices, consumers.getBuffer(RenderLayer.getLines()), box, r, g, b, 1f);
        if (consumers instanceof VertexConsumerProvider.Immediate imm) imm.draw(RenderLayer.getLines());
    }
}
