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
 *
 * Modified By Bi2Nb9O3
 * Modifications: to Yarn mapping
 */

package xyz.bi2nb9o3.minecartspeeddisplay.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.DisplayManager;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin
{
    @Inject(
        // lambda method in addLateDebugPass
        method = "method_75413",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/WorldRenderer;gizmos:Lnet/minecraft/client/render/WorldRenderer$Gizmos;",
            ordinal = 0
        )
    )
    private void render(
        CallbackInfo ci,
        @Local MatrixStack matrices
    )
    {
        DisplayManager.getInstance().render(
            matrices,
            0  // actually this is unused
        );
    }
}
