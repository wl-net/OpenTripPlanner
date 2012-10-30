package org.opentripplanner.analyst.request;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.opentripplanner.analyst.parameter.Style;

/** Color maps used with 8-bit travel time images. */
public class ColorModels {

    public static final IndexColorModel COLOR30 = buildDefaultColorMap();
    public static final IndexColorModel DIFFERENCE = buildDifferenceColorMap();
    public static final IndexColorModel TRANSPARENT = buildTransparentColorMap();
    public static final IndexColorModel MASK60 = buildMaskColorMap(60);
    public static final IndexColorModel MASK90 = buildMaskColorMap(90);
    public static final IndexColorModel BOARDINGS = buildBoardingColorMap();
    
    public static final Map<Style, IndexColorModel> modelsByStyle; 
    static {
        modelsByStyle = new EnumMap<Style, IndexColorModel>(Style.class);
        modelsByStyle.put(Style.COLOR30, COLOR30);
        modelsByStyle.put(Style.DIFFERENCE, DIFFERENCE);
        modelsByStyle.put(Style.TRANSPARENT, TRANSPARENT);
        modelsByStyle.put(Style.MASK60, MASK60);
        modelsByStyle.put(Style.MASK90, MASK90);
        modelsByStyle.put(Style.BOARDINGS, BOARDINGS);
    }

    public static IndexColorModel forStyle(Style style) {
        return modelsByStyle.get(style);
    }
    
    /* COLOR MAP BUILDER METHODS */
    
    private static IndexColorModel buildDefaultColorMap() {
        Color[] palette = new Color[256];
        final int ALPHA = 0x60FFFFFF; // ARGB
        for (int i = 0; i < 28; i++) {
                // Note: HSB = Hue / Saturation / Brightness
                palette[i + 00] =  new Color(ALPHA & Color.HSBtoRGB(0.333f, i * 0.037f, 0.8f), true); // Green
                palette[i + 30] =  new Color(ALPHA & Color.HSBtoRGB(0.666f, i * 0.037f, 0.8f), true); // Blue
                palette[i + 60] =  new Color(ALPHA & Color.HSBtoRGB(0.144f, i * 0.037f, 0.8f), true); // Yellow
                palette[i + 90] =  new Color(ALPHA & Color.HSBtoRGB(0.000f, i * 0.037f, 0.8f), true); // Red
                palette[i + 120] = new Color(ALPHA & Color.HSBtoRGB(0.000f, 0.000f, (29 - i) * 0.0172f), true); // Black
        }
        for (int i = 28; i < 30; i++) {
                palette[i + 00] =  new Color(ALPHA & Color.HSBtoRGB(0.333f, (30 - i) * 0.333f, 0.8f), true); // Green
                palette[i + 30] =  new Color(ALPHA & Color.HSBtoRGB(0.666f, (30 - i) * 0.333f, 0.8f), true); // Blue
                palette[i + 60] =  new Color(ALPHA & Color.HSBtoRGB(0.144f, (30 - i) * 0.333f, 0.8f), true); // Yellow
                palette[i + 90] =  new Color(ALPHA & Color.HSBtoRGB(0.000f, (30 - i) * 0.333f, 0.8f), true); // Red
                palette[i + 120] = new Color(ALPHA & Color.HSBtoRGB(0.000f, 0.000f, (29 - i) * 0.0172f), true); // Black
        }
        for (int i = 150; i < palette.length; i++) {
                palette[i] = new Color(0x00000000, true);
        }
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        byte[] a = new byte[256];
        for (int i = 0; i < palette.length; i++) {
                r[i] = (byte)palette[i].getRed();
                g[i] = (byte)palette[i].getGreen();
                b[i] = (byte)palette[i].getBlue();
                a[i] = (byte)palette[i].getAlpha();
        }
        return new IndexColorModel(8, 256, r, g, b, a);
    }

    private static IndexColorModel buildOldDefaultColorMap() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        byte[] a = new byte[256];
        Arrays.fill(a, (byte)0);
        for (int i=0; i<30; i++) {
            g[i + 00]  =  // <  30 green 
            a[i + 00]  =  
            b[i + 30]  =  // >= 30 blue
            a[i + 30]  =  
            g[i + 60]  =  // >= 60 yellow 
            r[i + 60]  =
            a[i + 60]  =  
            r[i + 90]  =  // >= 90 red
            a[i + 90]  =  
            b[i + 120] =  // >=120 pink fading to transparent 
            a[i + 120] =  
            r[i + 120] = (byte) (255 - (42 - i) * 6);
        }
        return new IndexColorModel(8, 256, r, g, b, a);
    }
    
    private static IndexColorModel buildDifferenceColorMap() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        byte[] a = new byte[256];
        Arrays.fill(a, (byte) 64);
        for (int i=0; i<118; i++) {
            g[117 - i] = (byte) (i * 2);
            r[137 + i] = (byte) (i * 2);
            a[117 - i] = (byte) (64+(i * 2));
            a[137 + i] = (byte) (64+(i * 2));
            //b[128 + i] = g[128 - i] = (byte)120; 
        }
//        for (int i=0; i<10; i++) {
//            byte v = (byte) (255 - i * 25);
//            g[127 - i] = v;
//            g[128 + i] = v;
//        }
//        a[255] = 64;
        return new IndexColorModel(8, 256, r, g, b, a);
    }

    private static IndexColorModel buildTransparentColorMap() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        byte[] a = new byte[256];
        for (int i=0; i<60; i++) {
            int alpha = 240 - i * 4;
            a[i] = (byte) alpha;
        }
        for (int i=60; i<255; i++) {
            a[i] = 0;
        }
        a[255] = (byte) 240;
        return new IndexColorModel(8, 256, r, g, b, a);
    }

    private static IndexColorModel buildMaskColorMap(int max) {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        byte[] a = new byte[256];
        for (int i=0; i<max; i++) {
            int alpha = (i * 210 / max);
            a[i] = (byte) alpha;
        }
        for (int i=max; i<=255; i++) {
            a[i] = (byte)210;
//            r[i] = (byte)255;
//            g[i] = (byte)128;
//            b[i] = (byte)128;
        }
        //a[255] = (byte) 240;
        return new IndexColorModel(8, 256, r, g, b, a);
    }

    private static IndexColorModel buildBoardingColorMap() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        byte[] a = new byte[256];
        Arrays.fill(a, (byte) 80);
        g[0] = (byte) 255;
        b[1] = (byte) 255;
        r[2] = (byte) 255;
        g[2] = (byte) 255;
        r[3] = (byte) 255;
        a[255] = 0;
        return new IndexColorModel(8, 256, r, g, b, a);
    }

}
