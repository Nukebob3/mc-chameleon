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

    public static String subtitle;

    public static float timeSinceUpdate = 0;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        timeSinceUpdate+=deltaTracker.getGameTimeDeltaTicks();

        if (maxTime==0) return;

        graphics.pose().pushMatrix();
        float scale = 1.5f + (timeSinceUpdate<1?(timeSinceUpdate-1)*(timeSinceUpdate-1)/2f:0);
        graphics.pose().scale(scale);
        int timeWidth = (int) (Minecraft.getInstance().font.width(""+time)*scale);
        graphics.pose().translate((graphics.guiWidth()/2f-timeWidth/2f)/scale, 40/scale);
        graphics.text(Minecraft.getInstance().font, ""+time, 0, 0, 0xFFFFFFFF);
        graphics.pose().popMatrix();
        graphics.text(Minecraft.getInstance().font, ""+whistle, graphics.guiWidth()/2+10, 26, 0xFFFFFF00);

        int frame = Math.round(9f*((float)(maxTime-time)/(float)maxTime));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("game/clock/"+(frame)), graphics.guiWidth()/2-12, 10, 24, 24);

        int subtitleWidth = Minecraft.getInstance().font.width(subtitle);
        graphics.text(Minecraft.getInstance().font, subtitle, graphics.guiWidth()/2-subtitleWidth/2, 55, 0xFFFFFFFF);

        for (int i = 0; i < hiders; i++) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("game/hider"), graphics.guiWidth()/2-8-32-(i)*16, 14, 16, 16);
        }
        for (int i = 0; i < seekers; i++) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("game/seeker"), graphics.guiWidth()/2-8+32+(i)*16, 14, 16, 16);
        }
    }
}
