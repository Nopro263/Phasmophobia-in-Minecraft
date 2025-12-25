package at.nopro.phasmo.lightingv3;

import java.util.Arrays;

public final class LightCompute {
    public static final byte[] EMPTY_CONTENT = new byte[2048];


    private LightCompute() {
    }

    public static byte[] bake(byte[] content1, byte[] content2) {
        if (content1 == null && content2 == null) {
            return EMPTY_CONTENT;
        } else if (content1 == EMPTY_CONTENT && content2 == EMPTY_CONTENT) {
            return EMPTY_CONTENT;
        } else if (content1 == null) {
            return content2;
        } else if (content2 == null) {
            return content1;
        } else if (Arrays.equals(content1, EMPTY_CONTENT) && Arrays.equals(content2, EMPTY_CONTENT)) {
            return EMPTY_CONTENT;
        } else {
            byte[] lightMax = new byte[2048];

            for (int i = 0; i < content1.length; ++i) {
                byte c1 = content1[i];
                byte c2 = content2[i];
                byte l1 = (byte) ( c1 & 15 );
                byte l2 = (byte) ( c2 & 15 );
                byte u1 = (byte) ( c1 >> 4 & 15 );
                byte u2 = (byte) ( c2 >> 4 & 15 );
                byte lower = (byte) Math.max(l1, l2);
                byte upper = (byte) Math.max(u1, u2);
                lightMax[i] = (byte) ( lower | upper << 4 );
            }

            return lightMax;
        }
    }
}
