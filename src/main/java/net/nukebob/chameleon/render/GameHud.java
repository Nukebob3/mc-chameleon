package net.nukebob.chameleon.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.nukebob.chameleon.MCChameleon;

public class GameHud {
    public static int hiders;
    public static int seekers;
    public static int time;
    public static int maxTime;
    public static int whistle;

    public static float timeSinceUpdate = 0;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        timeSinceUpdate+=deltaTracker.getGameTimeDeltaTicks();

        if (maxTime==0) return;

        graphics.pose().pushMatrix();
        int timeWidth = Minecraft.getInstance().font.width(""+time);
        graphics.text(Minecraft.getInstance().font, ""+time, graphics.guiWidth()/2-timeWidth/2, 40, 0xFFFFFFFF);
        graphics.text(Minecraft.getInstance().font, ""+whistle, graphics.guiWidth()/2+10, 26, 0xFFFFFF00);
        graphics.pose().popMatrix();

        int frame = Math.round(9f*((float)(maxTime-time)/(float)maxTime));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("game/clock/"+(frame)), graphics.guiWidth()/2-12, 10, 24, 24);
    }
}
