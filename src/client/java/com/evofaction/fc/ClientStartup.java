package com.evofaction.fc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;


public class ClientStartup implements ClientModInitializer {
    private static Box box = null;
    private static long expireAtMs = 0L;

    @Override
    public void onInitializeClient() {
        registerCommand();
        registerRenderer();
    }

    private void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("dab")
                    .then(ClientCommandManager.literal("clear").executes(ctx -> {
                        box = null;
                        expireAtMs = 0L;
                        ctx.getSource().sendFeedback(Text.literal("AABB cleared."));
                        return 1;
                    }))
                    .then(ClientCommandManager.argument("minx", DoubleArgumentType.doubleArg())
                        .then(ClientCommandManager.argument("miny", DoubleArgumentType.doubleArg())
                            .then(ClientCommandManager.argument("minz", DoubleArgumentType.doubleArg())
                                .then(ClientCommandManager.argument("maxx", DoubleArgumentType.doubleArg())
                                    .then(ClientCommandManager.argument("maxy", DoubleArgumentType.doubleArg())
                                        .then(ClientCommandManager.argument("maxz", DoubleArgumentType.doubleArg())
                                            // optional lifetime in ticks (default 1min)
                                            .executes(ctx -> setBox(ctx, 60 * 20))
                                            .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(1))
                                                .executes(ctx -> setBox(ctx, IntegerArgumentType.getInteger(ctx, "ticks")))
                                            ))))))));
        });
    }

    private int setBox(CommandContext<FabricClientCommandSource> ctx, int ticks) {
        double minx = DoubleArgumentType.getDouble(ctx, "minx");
        double miny = DoubleArgumentType.getDouble(ctx, "miny");
        double minz = DoubleArgumentType.getDouble(ctx, "minz");
        double maxx = DoubleArgumentType.getDouble(ctx, "maxx");
        double maxy = DoubleArgumentType.getDouble(ctx, "maxy");
        double maxz = DoubleArgumentType.getDouble(ctx, "maxz");

        // Normalize so min <= max even if user swaps them
        double nx1 = Math.min(minx, maxx);
        double ny1 = Math.min(miny, maxy);
        double nz1 = Math.min(minz, maxz);
        double nx2 = Math.max(minx, maxx);
        double ny2 = Math.max(miny, maxy);
        double nz2 = Math.max(minz, maxz);

        box = new Box(nx1, ny1, nz1, nx2, ny2, nz2).expand(0.002D);
        // convert ticks -> ms (20 tps)
        long lifeMs = (long) (ticks * 50L);
        expireAtMs = System.currentTimeMillis() + lifeMs;

        ctx.getSource().sendFeedback(Text.literal(
            String.format("Drawing AABB for %d ms: [%.3f, %.3f, %.3f] -> [%.3f, %.3f, %.3f]",
                lifeMs, nx1, ny1, nz1, nx2, ny2, nz2)
        ));
        return 1;
    }

    private void registerRenderer() {
        WorldRenderEvents.LAST.register(context -> {
            if (box == null) return;
            if (System.currentTimeMillis() > expireAtMs) {
                box = null;
                return;
            }

            MinecraftClient mc = MinecraftClient.getInstance();
            MatrixStack matrices = context.matrixStack();
            Camera cam = context.camera();
            Vec3d camPos = cam.getPos();

            matrices.push();
            // move world so camera is at origin
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);

            // line layer + consumer
            VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
            VertexConsumer lines = immediate.getBuffer(RenderLayer.getLines());

            RenderSystem.lineWidth(1.0f);

            // White, fully opaque
            float r = 1f, g = 1f, b = 0.2f, a = 1f;

            // Draw only the outline (no fill)
            WorldRenderer.drawBox(matrices, lines, box, r, g, b, a);

            matrices.pop();
            immediate.draw();
        });
    }
}
