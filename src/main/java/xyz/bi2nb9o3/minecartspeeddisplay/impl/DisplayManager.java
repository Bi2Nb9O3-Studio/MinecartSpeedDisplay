package xyz.bi2nb9o3.minecartspeeddisplay.impl;


import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class DisplayManager {

    private static final DisplayManager INSTANCE = new DisplayManager();
    private boolean TOGGLE = true;

    public static DisplayManager getInstance(){
        return INSTANCE;
    }

    public void toggle(MinecraftClient client){
        TOGGLE = !TOGGLE;
        client.inGameHud.setOverlayMessage(Text.translatable(TOGGLE ? "text.minecartspeeddisplay.toggle.on" :"text.minecartspeeddisplay.toggle.off"),false);
    }

}
