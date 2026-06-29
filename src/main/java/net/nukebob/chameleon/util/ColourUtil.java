package net.nukebob.chameleon.util;

import java.awt.*;

public class ColourUtil {
    public static int getPureHue(int colour) {
        if (colour==0xFFFFFFFF) return 0;

        int r = colour & 0xFF;
        int g = (colour >> 8) & 0xFF;
        int b = (colour >> 16) & 0xFF;

        float[] hsv = Color.RGBtoHSB(r, g, b, null);

        float hue = hsv[0];
        float maxSaturation = 1.0f;
        float maxValue = 1.0f;

        int rgb = Color.HSBtoRGB(hue, maxSaturation, maxValue);

        int awtR = (rgb >> 16) & 0xFF;
        int awtG = (rgb >> 8) & 0xFF;
        int awtB = rgb & 0xFF;
        return 0xFF000000 | (awtB << 16) | (awtG << 8) | awtR;
    }

    public static int getMaxValue(int colour) {
        if (colour == 0xFFFFFFFF) return 0xFFFFFFFF;

        int r = (colour >> 16) & 0xFF;
        int g = (colour >> 8) & 0xFF;
        int b = colour & 0xFF;

        float[] hsv = Color.RGBtoHSB(r, g, b, null);

        float hue = hsv[0];
        float maxSaturation = hsv[1];
        float maxValue = 1.0f;

        int rgb = Color.HSBtoRGB(hue, maxSaturation, maxValue);

        int awtR = (rgb >> 16) & 0xFF;
        int awtG = (rgb >> 8) & 0xFF;
        int awtB = rgb & 0xFF;

        return (0xFF << 24) | (awtR << 16) | (awtG << 8) | awtB;
    }

    public static int rgbToInt(int r, int g, int b) {
        return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static float[] getSaturationAndValue(int colour) {
        int r = colour & 0xFF;
        int g = (colour >> 8) & 0xFF;
        int b = (colour >> 16) & 0xFF;

        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float saturation = max < 1e-6f ? 0f : (delta / max) * 100f;
        float value = max * 100f;

        return new float[]{saturation, value};
    }
}
