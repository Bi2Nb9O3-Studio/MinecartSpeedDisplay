/*
 * This file is part of the Pistorder project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
 *
 * Pistorder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pistorder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pistorder.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.bi2nb9o3.minecartspeeddisplay.impl;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.StringVisitable;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class StringDrawer {
    private static final double MAX_RENDER_DISTANCE = (double)256.0F;
    private static final float FONT_SIZE = 0.025F;

    private static VertexConsumerProvider.Immediate getVertexConsumer() {
        return MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
    }

    public static void drawString(MatrixStack matrixStack, Vec3d pos, float tickDelta, float line, String[] texts, int[] colors) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null) {
            double x = (double)pos.getX() + (double)0.5F;
            double y = (double)pos.getY() + (double)0.5F;
            double z = (double)pos.getZ() + (double)0.5F;
            if (client.player.squaredDistanceTo(x, y, z) > (double)65536.0F) {
                return;
            }

            double camX = camera.getCameraPos().x;
            double camY = camera.getCameraPos().y;
            double camZ = camera.getCameraPos().z;
            matrixStack.push();
            matrixStack.translate((float)(x - camX), (float)(y - camY), (float)(z - camZ));
            matrixStack.multiplyPositionMatrix((new Matrix4f()).rotation(camera.getRotation()));
            matrixStack.scale(0.025F, -0.025F, 1.0F);
            GlStateManager._disableDepthTest();
            float totalWidth = 0.0F;

            for(String text : texts) {
                totalWidth += (float)client.textRenderer.getWidth(text);
            }

            float writtenWidth = 0.0F;

            for(int i = 0; i < texts.length; ++i) {
                float renderX = -totalWidth * 0.5F + writtenWidth;
                float renderY = (float)client.textRenderer.getWrappedLinesHeight(StringVisitable.plain(texts[i]), Integer.MAX_VALUE) * (-0.5F + 1.25F * line);
                Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
                VertexConsumerProvider.Immediate immediate = getVertexConsumer();
                client.textRenderer.draw(texts[i], renderX, renderY, colors[i] | -16777216, false, positionMatrix, immediate, TextLayerType.NORMAL, 0, 15728880);
                immediate.draw();
                writtenWidth += (float)client.textRenderer.getWidth(texts[i]);
            }

            GlStateManager._enableDepthTest();
            matrixStack.pop();
        }

    }
}
