package net.nukebob.chameleon.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.nukebob.chameleon.MCChameleon;

import java.util.UUID;

public class ChameleonTexture {
    private Identifier textureId;
    private DynamicTexture dynamicTexture;
    private NativeImage image;

    private final UUID uuid;

    public ChameleonTexture(UUID uuid) {
        this.uuid = uuid;
    }

    public Identifier getTextureId() {
        if (textureId == null) {
            init();
        }
        return textureId;
    }

    public void init() {
        image = new NativeImage(64, 64, true);

        for (int i = 0; i < 1504; i++) {
            Pixel pixel = pixelIndex(i);
            if (pixel.x>63||pixel.y>63) {
                continue;
            }
            image.setPixel(pixel.x, pixel.y, 0xFFFFFFFF);
        }

        dynamicTexture = new DynamicTexture(() -> "mc-chameleon-skin_"+uuid.toString(), image);

        textureId = MCChameleon.idSkin(uuid.toString());
    }

    public void load() {
        this.textureId = MCChameleon.idSkin(uuid.toString());
        if (Minecraft.getInstance().getTextureManager().getTexture(textureId) instanceof DynamicTexture existingTexture) {
            this.image = existingTexture.getPixels();

            this.dynamicTexture = existingTexture;
        }

        else init();
    }

    public void upload() {
        Minecraft.getInstance()
                .getTextureManager()
                .register(textureId, dynamicTexture);
    }

    public void updatePixels(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            Pixel pixel = pixelIndex(i);
            image.setPixel(pixel.x, pixel.y, pixels[i]);
        }
        upload();
    }

    public void updatePixelsSpecific(ColourLocation.ColLoc[] pixels) {
        for (ColourLocation.ColLoc colourLocation : pixels) {
            Pixel pixel = pixelIndex(colourLocation.location());
            image.setPixel(pixel.x, pixel.y, colourLocation.colour());
        }
        upload();
    }

    public void updatePixel(ColourLocation.ColLoc colourLocation) {
        Pixel pixel = pixelIndex(colourLocation.location());
        image.setPixel(pixel.x, pixel.y, colourLocation.colour());

        upload();
    }

    public static Pixel pixelIndex(int i) {
        int part = getPart(i);
        int localIndex = getLocalIndex(i, part);
        int face = getFace(part, localIndex); //0-5
        Pixel faceDimension = getFaceDimension(part,face);
        int pos = getFaceLocalIndex(localIndex, face, part);

        Pixel faceOffset = getFaceOffset(part, face);

        Pixel posOnFace = new Pixel(pos%faceDimension.x,pos/faceDimension.x);
        return faceOffset.add(posOnFace);
    }

    private static int getPart(int i) {
        if (i<8*8*2+4*8*2+8*4*2) {
            return 0;
        } else if (i<8*8*2+4*8*2+8*4*2 + 8*12*2+4*12*2+4*8*2) {
            return 1;
        } else {
            return 2+(i-(8*8*2+4*8*2+8*4*2 + 8*12*2+4*12*2+4*8*2))/(4*4*2+4*12*4);
        }
    }

    private static int getLocalIndex(int i, int part) {
        int localIndex = i;
        if (part >= 1) localIndex -= 8*8*2+4*8*2+8*4*2;
        if (part >= 2) localIndex -= 8*12*2 + 4*12*2 + 4*8*2;
        if (part >= 3) localIndex -= 4*4*2 + 4*12*4;
        if (part >= 4) localIndex -= 4*4*2 + 4*12*4;
        if (part == 5) localIndex -= 4*4*2 + 4*12*4;
        return localIndex;
    }

    private static int getFaceLocalIndex(int i, int face, int part) {
        int localIndex = i;
        if (face >= 1) localIndex -= getFaceSize(part, 0);
        if (face >= 2) localIndex -= getFaceSize(part, 1);
        if (face >= 3) localIndex -= getFaceSize(part, 2);
        if (face >= 4) localIndex -= getFaceSize(part, 3);
        if (face == 5) localIndex -= getFaceSize(part, 4);
        return localIndex;
    }

    private static int getFace(int part, int localIndex) {
        int sizeSum = 0;
        for (int i=0; i<6; i++) {
            sizeSum+=getFaceSize(part,i);
            if (localIndex<sizeSum) return i;
        }
        return 0;
    }

    private static Pixel getFaceDimension(int part, int face) {
        return switch (part) {
            case 0 -> switch (face) {
                case 0,2 -> new Pixel(8,8);
                case 1,3 -> new Pixel(4,8);
                case 4,5 -> new Pixel(8,4);
                default -> new Pixel(1);
            };
            case 1 -> switch (face) {
                case 0,2 -> new Pixel(8,12);
                case 1,3 -> new Pixel(4,12);
                case 4,5 -> new Pixel(8,4);
                default -> new Pixel(1);
            };
            case 2,3,4,5 -> switch (face) {
                case 0,1,2,3 -> new Pixel(4,12);
                case 4,5 -> new Pixel(4,4);
                default -> new Pixel(1);
            };
            default -> new Pixel(1);
        };
    }

    private static int getFaceSize(int part, int face) {
        Pixel dimension = getFaceDimension(part, face);
        return dimension.x*dimension.y;
    }

    private static Pixel getFaceOffset(int part, int face) {
        Pixel partOffset = switch (part) {
            case 0 -> new Pixel(8,8);
            case 1 -> new Pixel(20,20);
            case 2 -> new Pixel(44,20);
            case 3 -> new Pixel(36,52);
            case 4 -> new Pixel(4,20);
            case 5 -> new Pixel(20,52);
            default -> new Pixel(0);
        };
        return partOffset.add(switch (face) {
            case 1 -> new Pixel(getFaceDimension(part, 0).x,0);
            case 2 -> new Pixel(getFaceDimension(part, 0).x+getFaceDimension(part, 1).x,0);
            case 3 -> new Pixel(-getFaceDimension(part, 3).x,0);
            case 4 -> new Pixel(0,-getFaceDimension(part,4).y);
            case 5 -> new Pixel(getFaceDimension(part, 4).x,-getFaceDimension(part,4).y);
            default -> new Pixel(0);
        });
    }
}
