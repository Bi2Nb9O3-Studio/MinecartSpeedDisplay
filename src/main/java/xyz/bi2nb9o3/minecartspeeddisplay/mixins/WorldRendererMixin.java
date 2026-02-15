package xyz.bi2nb9o3.minecartspeeddisplay.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.DisplayManager;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.MinecartTracer;

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

        // Render minecart trails
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.gameRenderer != null && client.gameRenderer.getCamera() != null) {
            double camX = client.gameRenderer.getCamera().getCameraPos().x;
            double camY = client.gameRenderer.getCamera().getCameraPos().y;
            double camZ = client.gameRenderer.getCamera().getCameraPos().z;
            MinecartTracer.getInstance().render(matrices, null, camX, camY, camZ);
        }
    }
}