package xyz.bi2nb9o3.minecartspeeddisplay.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.text.DecimalFormat;
import java.util.Objects;

public class SpeedDisplay {
    public final World WORLD;
    public final MinecartEntity ENTITY;
    DecimalFormat df = new DecimalFormat(".00");
    private boolean valid = true;

    public SpeedDisplay(World world, MinecartEntity entity) {
        WORLD = world;
        ENTITY = entity;
    }

    private static void drawString(MatrixStack matrixStack, Vec3d pos, float tickDelta, float line, String text, int color)
    {
        StringDrawer.drawString(matrixStack, pos, tickDelta, line, new String[]{text}, new int[]{color});
    }

    private boolean isDisabled(){
        return DisplayManager.getInstance().isDisabled();
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isInvalid(){
        return !valid;
    }

    public void valid(){
        valid = true;
    }

    public void invalid(){
        valid = false;
    }

    private boolean checkState(World world)
    {
        if(world == null){
            return false;
        }
        BlockView chunk = world.getChunkManager().getChunk(this.ENTITY.getBlockPos().getX() >> 4, this.ENTITY.getBlockPos().getZ() >> 4);
        return Objects.equals(world, this.WORLD) && (chunk instanceof WorldChunk && !((WorldChunk) chunk).isEmpty());
    }



    void render(MatrixStack matrixStack, float tickDelta)
    {
        if (!this.isDisabled())
        {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!this.checkState(client.world))
            {
                this.invalid();
                return;
            }
            drawString(matrixStack,
                this.ENTITY.getPos(),
                tickDelta,
                0.7F,
                String.format(
                    "X: %s block/s ",
                    String.valueOf(
                        df.format(ENTITY.getVelocity().x*20))),
                Formatting.AQUA.getColorValue());
            drawString(matrixStack, this.ENTITY.getPos(), tickDelta, 0, String.format("Y: %s block/s ",String.valueOf(df.format(ENTITY.getVelocity().y*20))),Formatting.GOLD.getColorValue());
            drawString(matrixStack, this.ENTITY.getPos(), tickDelta, -0.7F, String.format("Z: %s block/s ",String.valueOf(df.format(ENTITY.getVelocity().z*20))),Formatting.GREEN.getColorValue());

        }
    }
}
