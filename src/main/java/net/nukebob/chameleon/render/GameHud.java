package net.nukebob.chameleon.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.nukebob.chameleon.MCChameleon;

public class GameHud {
    public static int hiders;
    public static int seekers;
    public static int time;
    public static int maxTime;
    public static int whistle;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        graphics.text(Minecraft.getInstance().font, ""+time, graphics.guiWidth()/2, 20, 0xFFFFFFFF);
        graphics.text(Minecraft.getInstance().font, ""+maxTime, graphics.guiWidth()/2, 40, 0xFFFFFFFF);
        graphics.text(Minecraft.getInstance().font, ""+whistle, graphics.guiWidth()/2, 60, 0xFFFFFFFF);

        //graphics.blit(MCChameleon.id());
    }
}
