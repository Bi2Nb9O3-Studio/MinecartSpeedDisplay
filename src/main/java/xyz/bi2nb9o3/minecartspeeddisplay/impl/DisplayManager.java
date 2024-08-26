package xyz.bi2nb9o3.minecartspeeddisplay.impl;


import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class DisplayManager {

    private static final DisplayManager INSTANCE = new DisplayManager();
    private boolean TOGGLE = true;
    public final List<SpeedDisplay> displayMap = Collections.synchronizedList(Lists.newArrayList());

    public static DisplayManager getInstance(){
        return INSTANCE;
    }

    public void toggle(MinecraftClient client){
        TOGGLE = !TOGGLE;
        client.inGameHud.setOverlayMessage(Text.translatable(TOGGLE ? "text.minecartspeeddisplay.toggle.on" :"text.minecartspeeddisplay.toggle.off"),false);
    }

    public boolean isEnabled(){
        return TOGGLE;
    }

    public boolean isDisabled(){
        return !TOGGLE;
    }

    public void render(MatrixStack matrixStack, float tickDelta)
    {
        if (!this.isEnabled())
        {
//            this.displayMap.clear();
            return;
        }

        List<SpeedDisplay> removeList = Lists.newArrayList();
        this.displayMap.forEach(display -> {
            display.render(matrixStack, tickDelta);
            if (display.isInvalid())
            {
                removeList.add(display);
            }
        });
        removeList.forEach(this.displayMap::remove);
    }

    public void deleteEntity(World world, MinecartEntity entity){
//        displayMap.remove(new SpeedDisplay(world, entity));
        for(int i=0;i<displayMap.size();i++){
            if (displayMap.get(i).ENTITY==entity && displayMap.get(i).ENTITY.equals(entity)){
                displayMap.remove(i);
            }
        }
//        MinecartSpeedDisplay.LOGGER.info("Removed entity {} from display map", entity.toString());
//        MinecartSpeedDisplay.LOGGER.info("Display list:{}", displayMap);
    }

}
