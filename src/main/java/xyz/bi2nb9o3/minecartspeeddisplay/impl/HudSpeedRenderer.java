package xyz.bi2nb9o3.minecartspeeddisplay.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Formatting;
import xyz.bi2nb9o3.minecartspeeddisplay.MinecartSpeedDisplay;

import java.text.DecimalFormat;

public class HudSpeedRenderer {
    private static final DecimalFormat df = new DecimalFormat(".00");
    private static void renderSpeedHud(DrawContext drawContext, AbstractMinecartEntity minecart) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        drawContext.drawText(client.textRenderer, "TEST", 10, 10, 0xFFFFFF, true);

        // Calculate position (center of screen, above crosshair)
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int x = screenWidth / 2;
        int y = (screenHeight / 2 - 120) > 0 ? (screenHeight / 2 - 120) : 0; // Position above crosshair

        // Get speed values
        double speedX = minecart.getVelocity().x * 20;
        double speedY = minecart.getVelocity().y * 20;
        double speedZ = minecart.getVelocity().z * 20;
        double totalSpeed = Math.sqrt(speedX * speedX + speedY * speedY + speedZ * speedZ);

        // Render speed information
        drawCenteredText(drawContext, textRenderer, "Minecart Speed:", x, y, Formatting.WHITE.getColorValue());
        drawCenteredText(drawContext, textRenderer, "Total: " + df.format(totalSpeed) + " block/s", x, y + 12, Formatting.GREEN.getColorValue()+(255<<24));
        drawCenteredText(drawContext, textRenderer, "X: " + df.format(speedX) + " block/s", x, y + 24, Formatting.AQUA.getColorValue()+(255<<24));
        drawCenteredText(drawContext, textRenderer, "Y: " + df.format(speedY) + " block/s", x, y + 36, Formatting.GOLD.getColorValue()+(255<<24));
        drawCenteredText(drawContext, textRenderer, "Z: " + df.format(speedZ) + " block/s", x, y + 48, Formatting.GREEN.getColorValue()+(255<<24));
    }

    private static void drawCenteredText(DrawContext drawContext, TextRenderer textRenderer, String text, int x, int y, int color) {
        int textWidth = textRenderer.getWidth(text);
        drawContext.drawText(textRenderer, text, x - textWidth / 2, y, color, false);
    }

    public static void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        // Check if player is in a minecart
        if (client.player.getVehicle() instanceof AbstractMinecartEntity minecart) {
            DisplayManager manager = DisplayManager.getInstance();
            if (!manager.isDisabled()) {
                renderSpeedHud(drawContext, minecart);
            }
        }
    }
}