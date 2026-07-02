package net.nukebob.chameleon.util;

public class ColourUtil {
    public static int[] rgbToHsv(int r, int g, int b) {
        double rf = r / 255.0;
        double gf = g / 255.0;
        double bf = b / 255.0;

        double max = Math.max(rf, Math.max(gf, bf));
        double min = Math.min(rf, Math.min(gf, bf));
        double delta = max - min;

        double h = 0;
        double s = 0;
        double v = max;

        if (max != 0) {
            s = delta / max;
        } else {
            s = 0;
        }

        if (delta != 0) {
            if (max == rf) {
                h = (gf - bf) / delta + (gf < bf ? 6 : 0);
            } else if (max == gf) {
                h = (bf - rf) / delta + 2;
            } else if (max == bf) {
                h = (rf - gf) / delta + 4;
            }
            h /= 6;
        }

        int hInt = (int) Math.round(h * 360);
        int sInt = (int) Math.round(s * 100);
        int vInt = (int) Math.round(v * 100);

        return new int[]{hInt, sInt, vInt};
    }
    public static int[] rgbToHsv(int rgb) {
        int[] col = intToRgb(rgb);
        return rgbToHsv(col[0], col[1], col[2]);
    }
    public static int[] hsvToRgb(float h, float s, float v) {
        s /= 100f;
        v /= 100f;
        float c = v * s;
        float x = c * (1 - Math.abs(((h / 60f) % 2) - 1));
        float m = v - c;
        float r, g, b;
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        return new int[]{
                Math.round((r + m) * 255),
                Math.round((g + m) * 255),
                Math.round((b + m) * 255)
        };
    }

    public static int rgbToInt(int r, int g, int b) {
        return (0xFF << 24) | ((b & 0xFF) << 16) | ((g & 0xFF) << 8) | (r & 0xFF);
    }

    public static int hsvToInt(float h, float s, float v) {
        int[] rgb = hsvToRgb(h, s, v);
        return rgbToInt(rgb[0], rgb[1], rgb[2]);
    }

    public static int[] intToRgb(int colour) {
        int r = colour & 0xFF;
        int g = (colour >> 8) & 0xFF;
        int b = (colour >> 16) & 0xFF;

        return new int[]{r, g, b};
    }
}
