package de.inselhome.noteapp.util;


import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

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

    public static Spannable colorText(final String text) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(text);
        colorText(builder, text, "#");
        colorText(builder, text, "@");
        return builder;
    }

    private static Spannable colorText(final SpannableStringBuilder builder, final String text, final String keyChar) {
        int index = -1;

        do {
            index = colorText(builder, text, index + 1, keyChar);
        }
        while (index >= 0);

        return builder;
    }

    private static int colorText(final SpannableStringBuilder builder, final String text, final int start, final String keyChar) {
        final int index = text.indexOf(keyChar, start);

        if (index >= 0) {
            final int space = text.indexOf(" ", index);

            if (space > index) {
                final String substring = text.substring(index, space);
                builder.setSpan(new ForegroundColorSpan(ColorProvider.fromString(substring)), index, space, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                final String substring = text.substring(index, text.length());
                builder.setSpan(new ForegroundColorSpan(ColorProvider.fromString(substring)), index, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }

        return index;
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
