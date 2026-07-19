package net.nukebob.chameleon.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.gameplay.MissedSpot;
import net.nukebob.chameleon.gameplay.TeamControl;
import net.nukebob.chameleon.keybind.Keybinds;
import net.nukebob.chameleon.screen.PaintScreen;

import java.util.ArrayList;
import java.util.List;

public class GameHud {
    public static int hiders;
    public static int seekers;
    public static int time;
    public static int maxTime;
    public static int whistle;

    public static String subtitle;

    public static float timeSinceUpdate = 0;

    public static int missedSpotTimer;
    public static List<MissedSpot.MissedSpotEntry> missedSpots = new ArrayList<>();
    public static boolean missedSpotEnabledForHunter = true;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        timeSinceUpdate+=deltaTracker.getGameTimeDeltaTicks();

        if (maxTime==0) {
            missedSpots=new ArrayList<>();
            return;
        }

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

        if (!(Minecraft.getInstance().gui.screen() instanceof PaintScreen)) renderMissedSpot(graphics, deltaTracker);
    }

    private static void renderMissedSpot(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!subtitle.equals("Search Time")&!subtitle.equals("Answer Check")) return;
        if (Minecraft.getInstance().player==null||(TeamControl.isHunter(Minecraft.getInstance().player.getTeam())&&!missedSpotEnabledForHunter)) return;

        graphics.fill(1, graphics.guiHeight()/2-16, 13, graphics.guiHeight()/2-16+12, 0x55000000);
        ChameleonHud.renderKey(graphics, Keybinds.toggleMissedSpotRanking, 2, graphics.guiHeight()/2-15, 10, Keybinds.toggleMissedSpotRanking.isDown()?0xFFAAAAAA:0xFFFFFFFF, 0xFF000000);

        if (!MCChameleonClient.missedSpotRankingDisplay) return;

        Font font = Minecraft.getInstance().font;

        drawScaledText(graphics, font, "Missed-Spot Ranking", 1.15f, 15, graphics.guiHeight()/2-15, 0xFFFFFFFF);

        drawScaledText(graphics, font, "next update", 0.8f, (int) (10 + font.width("Missed-Spot Ranking")*1.2f+1), graphics.guiHeight()/2-15-5, 0xFFfcff33);
        graphics.text(font, ""+missedSpotTimer, (int) (10 + font.width("Missed-Spot Ranking")*1.2f+1+font.width("next update")*0.8f/2-font.width(""+missedSpotTimer)/2), graphics.guiHeight()/2-15+5, 0xFFfcff33);

        for (int i = 0; i < missedSpots.size(); i++) {
            MissedSpot.MissedSpotEntry missedSpot = missedSpots.get(i);
            int minX = 2+font.width("#"+missedSpot.ranking());
            int maxX = (int) (10 + font.width("Missed-Spot Ranking")*1.2f+1)+2;
            graphics.text(font, "#"+missedSpot.ranking(), 2, graphics.guiHeight()/2-15 + 12*(i)+15, 0xFFFFFFFF);
            graphics.text(font, ""+missedSpot.score(), maxX, graphics.guiHeight()/2-15 + 12*(i)+15, 0xFFFFFFFF);
            int colour = missedSpot.playerName().equals(Minecraft.getInstance().player==null?"":Minecraft.getInstance().player.getScoreboardName())?0xFF00FF00:(missedSpot.alive()?0xFFFFFFFF:0xFFFF0000);
            graphics.text(font, missedSpot.playerName(), (maxX+minX-font.width(missedSpot.playerName()))/2, graphics.guiHeight()/2-15 + 12*(i)+15, colour);
        }
    }

    private static void drawScaledText(GuiGraphicsExtractor graphics, Font font, String str, float scale, int x, int y, int colour) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale);
        graphics.pose().translate(x/scale, y/scale);
        graphics.text(font, str, 0,0, colour);
        graphics.pose().popMatrix();
    }
}
