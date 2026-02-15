package xyz.bi2nb9o3.minecartspeeddisplay;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.DisplayManager;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.HudSpeedRenderer;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.MinecartTracer;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.SpeedDisplay;

public class MinecartSpeedDisplay implements ModInitializer{
    public static final Logger LOGGER =
        LogManager.getLogger();

    public static final String MOD_ID = "minecartspeeddisplay";
    public static String MOD_VERSION = "unknown";
    public static String MOD_NAME = "MinecartSpeedDisplay";
    public static KeyBinding.Category CATEGORY = new KeyBinding.Category(
        Identifier.of(MOD_ID, "custom_category")
    );
    public static final KeyBinding TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.minecartspeeddisplay.toggle",
        InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
        GLFW.GLFW_KEY_P, // The keycode of the key
        CATEGORY
//        "category.minecartspeeddisplay.text" // The translation key of the keybinding's category.
    ));

    // New keybindings for trail tracking
    public static final KeyBinding START_TRACKING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.minecartspeeddisplay.start_tracking",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_BACKSLASH, // \ key
        CATEGORY
    ));

    public static final KeyBinding STOP_TRACKING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.minecartspeeddisplay.stop_tracking",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_LEFT_BRACKET, // [ key
        CATEGORY
    ));

    public static final KeyBinding CLEAR_TRAILS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.minecartspeeddisplay.clear_trails",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_RIGHT_BRACKET, // ] key
        CATEGORY
    ));

    @Override
    public void onInitialize() {
        ModMetadata metadata = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
        MOD_NAME = metadata.getName();
        MOD_VERSION = metadata.getVersion().getFriendlyString();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE.wasPressed()){
                DisplayManager.getInstance().toggle(client);
            }

            // Handle trail tracking keybindings
            while (START_TRACKING.wasPressed()) {
                handleStartTracking(client);
            }

            while (STOP_TRACKING.wasPressed()) {
                handleStopTracking(client);
            }

            while (CLEAR_TRAILS.wasPressed()) {
                handleClearTrails(client);
            }

            // Update minecart tracers
            MinecartTracer.getInstance().update();
        });
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof AbstractMinecartEntity minecartEntity) {
                DisplayManager.getInstance().displayMap.add(new SpeedDisplay(world, minecartEntity));
            }
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof AbstractMinecartEntity minecartEntity) {
                DisplayManager.getInstance().deleteEntity(world, minecartEntity);
            }
        });

        // Register HUD render callback
        // 修改 HUD 渲染回调部分
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            // Render speed HUD
            new HudSpeedRenderer().onHudRender(drawContext, tickDelta);
            // Render trail info
            MinecartTracer.getInstance().renderHud(drawContext);
        });
    }

    private static void handleStartTracking(MinecraftClient client) {
        if (client.player != null) {
            // Get looking at minecart
            AbstractMinecartEntity minecart = getLookingAtMinecart(client);
            if (minecart != null) {
                MinecartTracer.getInstance().startTracking(minecart);
                client.inGameHud.setOverlayMessage(Text.literal("Started tracking minecart trail"), false);
            } else {
                client.inGameHud.setOverlayMessage(Text.literal("No minecart in sight"), false);
            }
        }
    }

    private static void handleStopTracking(MinecraftClient client) {
        MinecartTracer.getInstance().stopTracking();
        client.inGameHud.setOverlayMessage(Text.literal("Stopped tracking minecart trails"), false);
    }

    private static void handleClearTrails(MinecraftClient client) {
        MinecartTracer.getInstance().clearAllTrails();
        client.inGameHud.setOverlayMessage(Text.literal("Cleared all minecart trails"), false);
    }

    private static AbstractMinecartEntity getLookingAtMinecart(MinecraftClient client) {
        if (client.player == null) return null;

        HitResult hit = client.crosshairTarget;
        switch(hit.getType()) {
            case HitResult.Type.MISS:
                //nothing near enough
                break;
            case HitResult.Type.BLOCK:
                break;
            case HitResult.Type.ENTITY:
                EntityHitResult entityHit = (EntityHitResult) hit;
                Entity entity = entityHit.getEntity();
                if (entity != null && entity instanceof AbstractMinecartEntity minecartEntity) {
                    return minecartEntity;
                }
                break;
        }
        return null;
    }
}