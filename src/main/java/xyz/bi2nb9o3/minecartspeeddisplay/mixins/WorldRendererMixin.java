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

package xyz.bi2nb9o3.minecartspeeddisplay.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.DisplayManager;
//#endif

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin
{
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/Camera;)V"
        )
    )
    private void renderPistorder(
        CallbackInfo ci,
        @Local(
            //#if MC < 12005
            //$$ argsOnly = true
            //#endif
        ) MatrixStack matrices,

        @Local(argsOnly = true)
        //#if MC >= 12100
        RenderTickCounter tickCounter
        //#else
        //$$ float tickDelta
        //#endif
    )
    {
        DisplayManager.getInstance().render(
            matrices,
            //#if MC >= 12100
            tickCounter.getTickDelta(false)
            //#else
            //$$ tickDelta
            //#endif
        );
    }
}
