package de.interoberlin.merlot_android.model.parser.data;

public class LightColorProx {
    // <editor-fold defaultstate="collapsed" desc="Members">

    // public long ts;     //"ts":1400776389653,
    // public long light;  //"light":65535,                           //format: 16 bit unsigned, range: 0-65535
    // public Color clr;   //"clr":{"r":65535,"g":65535,"b":65535}, //format: 16 bit unsigned, range: 0-65535
    // public long prox;   //"prox":65535

    public static class Color {
        public final int red;
        public final int green;
        public final int blue;

        public Color(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        /**
         * @return a color-int from red, green, blue components.
         * The alpha component is implicitly 255 (fully opaque).
         * These component values should be [0..255], but there is no
         * range check performed, so if they are out of range, the
         * returned color is undefined.
         * <pre> {@code
         * <p>
         * int rgb = color.toRgb();
         * Bitmap image = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
         * image.eraseColor(c);
         * </p>
         * } </pre>
         */
        public int toRgb() {
            float rr = red;
            float gg = green;
            float bb = blue;

            //relative correction
            rr *= 2.0 / 3.0;

            //normalize
            float max = Math.max(rr, Math.max(gg, bb));
            if (max == 0) max = 1;
            rr = (rr / max) * 255;
            gg = (gg / max) * 255;
            bb = (bb / max) * 255;

            return (0xFF << 24) | ((int) rr << 16) | ((int) gg << 8) | (int) bb;
        }
    }

    // </editor-fold>
}
