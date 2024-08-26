package xyz.bi2nb9o3.minecartspeeddisplay;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.vehicle.MinecartEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.DisplayManager;
import xyz.bi2nb9o3.minecartspeeddisplay.impl.SpeedDisplay;

public class MinecartSpeedDisplay implements ModInitializer{
    public static final Logger LOGGER =
        LogManager.getLogger();

    public static final String MOD_ID = "minecartspeeddisplay";
    public static String MOD_VERSION = "unknown";
    public static String MOD_NAME = "unknown";
    public static final KeyBinding TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.minecartspeeddisplay.toggle",
        InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
        GLFW.GLFW_KEY_P, // The keycode of the key
        "category.minecartspeeddisplay.text" // The translation key of the keybinding's category.
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
        });
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof MinecartEntity minecartEntity) {
                DisplayManager.getInstance().displayMap.add(new SpeedDisplay(world, minecartEntity));
            }
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof MinecartEntity minecartEntity) {
                DisplayManager.getInstance().deleteEntity(world, minecartEntity);
            }
        });
    }
}
