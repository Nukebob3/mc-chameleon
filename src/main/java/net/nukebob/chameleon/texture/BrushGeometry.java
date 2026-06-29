package net.nukebob.chameleon.texture;

public class BrushGeometry {
    private static final int TEXEL_COUNT = 1504;

    private static final float[][] REST_POSITIONS = new float[TEXEL_COUNT][];

    //minX, minY, minZ, width, height, depth
    private static final float[][] BOXES = {
            {-4, -8, -4, 8, 8, 8},   //0 head
            {-4, 0, -2, 8, 12, 4},   //1 body
            {-3, -2, -2, 4, 12, 4},  //2 right_arm
            {-1, -2, -2, 4, 12, 4},  //3 left_arm
            {-2, 0, -2, 4, 12, 4},   //4 right_leg
            {-2, 0, -2, 4, 12, 4},   //5 left_leg
    };

    //pivot
    private static final float[][] PIVOTS = {
            {0, 0, 0},      //0 head
            {0, 0, 0},      //1 body
            {-5, 2, 0},     //2 right_arm
            {5, 2, 0},      //3 left_arm
            {-1.9f, 12, 0}, //4 right_leg
            {1.9f, 12, 0},  //5 left_leg
    };

    //0->north
    private static final String[] FACE_NAMES = {"NORTH", "EAST", "SOUTH", "WEST", "UP", "DOWN"};

    static {
        for (int i = 0; i < TEXEL_COUNT; i++) {
            REST_POSITIONS[i] = computeRestPosition(i);
        }
    }

    private BrushGeometry() {}

    public static float[] getRestPosition(int texelIndex) {
        if (texelIndex < 0 || texelIndex >= TEXEL_COUNT) return null;
        return REST_POSITIONS[texelIndex];
    }

    public static int getTexelCount() {
        return TEXEL_COUNT;
    }

    private static float[] computeRestPosition(int i) {
        int part = ChameleonTexture.getPart(i);
        int localIndex = ChameleonTexture.getLocalIndex(i, part);
        int face = ChameleonTexture.getFace(part, localIndex);
        int pos = ChameleonTexture.getFaceLocalIndex(localIndex, face, part);

        Pixel faceDim = ChameleonTexture.getFaceDimension(part, face);
        int col = pos % faceDim.x;
        int row = pos / faceDim.x;
        float tCol = (col + 0.5f) / faceDim.x;
        float tRow = (row + 0.5f) / faceDim.y;

        float[] box = BOXES[part];
        float minX = box[0], minY = box[1], minZ = box[2];
        float w = box[3], h = box[4], d = box[5];
        float maxX = minX + w, maxY = minY + h, maxZ = minZ + d;

        float lx = 0, ly = 0, lz = 0;
        String faceName = FACE_NAMES[face];

        switch (faceName) {
            case "WEST" -> {
                lx = minX;
                ly = minY + tRow * h;
                lz = maxZ - tCol * d;
            }
            case "NORTH" -> {
                lx = minX + tCol * w;
                ly = minY + tRow * h;
                lz = minZ;
            }
            case "EAST" -> {
                lx = maxX;
                ly = minY + tRow * h;
                lz = minZ + tCol * d;
            }
            case "SOUTH" -> {
                lx = maxX - tCol * w;
                ly = minY + tRow * h;
                lz = maxZ;
            }
            case "UP" -> {
                lx = minX + tCol * w;
                ly = minY;
                lz = minZ + tRow * d;
            }
            case "DOWN" -> {
                lx = minX + tCol * w;
                ly = maxY;
                lz = maxZ - tRow * d;
            }
        }

        float[] pivot = PIVOTS[part];
        return new float[]{lx + pivot[0], ly + pivot[1], lz + pivot[2]};
    }

    public static float distance(int texelA, int texelB) {
        float[] a = getRestPosition(texelA);
        float[] b = getRestPosition(texelB);
        if (a == null || b == null) return Float.MAX_VALUE;
        float dx = a[0] - b[0], dy = a[1] - b[1], dz = a[2] - b[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
