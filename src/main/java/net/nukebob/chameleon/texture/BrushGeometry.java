package net.nukebob.chameleon.texture;

import net.minecraft.core.Vec3i;
import net.nukebob.chameleon.gameplay.Poses;

public class BrushGeometry {
    private static final int TEXEL_COUNT = 1504;

    private static final float[][] REST_POSITIONS = new float[TEXEL_COUNT][];

    //minX, minY, minZ, width, height, depth
    private static final float[][] BOXES = {
            {-4, -8, -4, 8, 8, 8},   //head
            {-4, 0, -2, 8, 12, 4},   //body
            {-4, -2, -2, 4, 12, 4},  //right_arm
            {0,  -2, -2, 4, 12, 4},  //left_arm
            {-2, 0, -2, 4, 12, 4},   //right_leg
            {-2, 0, -2, 4, 12, 4},   //left_leg
    };

    //pivot
    private static final float[][] PIVOTS = {
            {0, 0, 0},      //head
            {0, 0, 0},      //body
            {5, -2, 0},     //right_arm
            {-5, -2, 0},    //left_arm
            {-1.9f, 12, 0}, //right_leg
            {1.9f, 12, 0},  //left_leg
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
                lz = maxZ - tRow * d;
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

    public static float[] getPosedPosition(int texelIndex, Poses pose) {
        if (texelIndex < 0 || texelIndex >= TEXEL_COUNT) return null;
        int part = ChameleonTexture.getPart(texelIndex);
        float[] rest = REST_POSITIONS[texelIndex];

        float[] pivot = PIVOTS[part];

        float lx = rest[0] - pivot[0];
        float ly = rest[1] - pivot[1];
        float lz = rest[2] - pivot[2];

        Vec3i partRot = getPartRot(part, pose);
        float[] rotated = rotateXYZ(lx, ly, lz, partRot.getX(), partRot.getY(), partRot.getZ());

        rotated[0] += pivot[0];
        rotated[1] += pivot[1];
        rotated[2] += pivot[2];

        //waist rotation
        if (part == 4 || part == 5) {
            Vec3i waist = pose.getWaist();
            if (waist.getX() != 0 || waist.getY() != 0 || waist.getZ() != 0) {
                float wx = rotated[0];
                float wy = rotated[1] - 12f;
                float wz = rotated[2];
                float[] waistRotated = rotateXYZ(wx, wy, wz, waist.getX(), waist.getY(), waist.getZ());
                rotated[0] = waistRotated[0];
                rotated[1] = waistRotated[1] + 12f;
                rotated[2] = waistRotated[2];
            }
        }

        //root offset
        Vec3i rootOffset = pose.getRootOffset();
        rotated[0] += rootOffset.getX();
        rotated[1] += rootOffset.getY();
        rotated[2] += rootOffset.getZ();

        //root rotation
        Vec3i rootRot = pose.getRootRot();
        if (rootRot.getX() != 0 || rootRot.getY() != 0 || rootRot.getZ() != 0) {
            rotated = rotateXYZ(rotated[0], rotated[1], rotated[2],
                    rootRot.getX(), rootRot.getY(), rootRot.getZ());
        }

        return rotated;
    }

    private static Vec3i getPartRot(int part, Poses pose) {
        return switch (part) {
            case 0 -> pose.getHead();
            case 1 -> pose.getTorso();
            case 2 -> pose.getRightArm();
            case 3 -> pose.getLeftArm();
            case 4 -> pose.getRightLeg();
            case 5 -> pose.getLeftLeg();
            default -> Vec3i.ZERO;
        };
    }

    private static float[] rotateXYZ(float x, float y, float z, int rx, int ry, int rz) {
        float[] v = {x, y, z};

        if (rx != 0) {
            float rad = (float) Math.toRadians(rx);
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);
            float ny = v[1] * cos - v[2] * sin;
            float nz = v[1] * sin + v[2] * cos;
            v[1] = ny;
            v[2] = nz;
        }
        if (ry != 0) {
            float rad = (float) Math.toRadians(ry);
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);
            float nx = v[0] * cos + v[2] * sin;
            float nz = -v[0] * sin + v[2] * cos;
            v[0] = nx;
            v[2] = nz;
        }
        if (rz != 0) {
            float rad = (float) Math.toRadians(rz);
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);
            float nx = v[0] * cos - v[1] * sin;
            float ny = v[0] * sin + v[1] * cos;
            v[0] = nx;
            v[1] = ny;
        }

        return v;
    }

    public static float distance(int texelA, int texelB) {
        float[] a = getRestPosition(texelA);
        float[] b = getRestPosition(texelB);
        if (a == null || b == null) return Float.MAX_VALUE;
        float dx = a[0] - b[0], dy = a[1] - b[1], dz = a[2] - b[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static float posedDistance(int texelA, int texelB, Poses pose) {
        float[] a = getPosedPosition(texelA, pose);
        float[] b = getPosedPosition(texelB, pose);
        if (a == null || b == null) return Float.MAX_VALUE;
        float dx = a[0] - b[0], dy = a[1] - b[1], dz = a[2] - b[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}