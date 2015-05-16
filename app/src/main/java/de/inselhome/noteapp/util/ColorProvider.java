package de.inselhome.noteapp.util;


import android.graphics.Color;

/**
 * @author iweinzierl
 */
public class ColorProvider {

    private static class Triple {
        public int r;
        public int g;
        public int b;

        private Triple(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public static int fromString(String str) {
        final Triple triple = tripleFromString(str);

        return Color.rgb(triple.r % 255, triple.g % 255, triple.b % 255);
    }

    private static Triple tripleFromString(String str) {
        if (str.length() > 2) {
            final float tokenLength = str.length() / 3;
            final String tokenR = extractToken(str, tokenLength, 0);
            final String tokenG = extractToken(str, tokenLength, 1);
            final String tokenB = extractToken(str, tokenLength, 2);

            return new Triple(tokenR.hashCode(), tokenG.hashCode(), tokenB.hashCode());
        }

        return new Triple(0, 0, 0);
    }

    private static String extractToken(String str, float tokenLength, int i) {
        int start = (int) tokenLength * i;
        int end = (int) tokenLength * (i + 1);

        return str.substring(start, end);
    }
}
