package sample;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

public class Converter {

    private static int[] CMYK_RGB(double c, double m, double y, double k) {
        return new int[]{
                (int) (255 * (1 - c) * (1 - k)),
                (int) (255 * (1 - m) * (1 - k)),
                (int) (255 * (1 - y) * (1 - k))
        };
    }

    private static double[] RGB_CMYK(int r, int g, int b) {
        int k = Stream.of(1 - r / 255, 1 - g / 255, 1 - b / 255).min(Comparator.comparingInt(a -> a)).get();

        return new double[]{
                (double) (1 - r / 255 - k) / (1 - k),
                (double) (1 - g / 255 - k) / (1 - k),
                (double) (1 - b / 255 - k) / (1 - k),
                k
        };
    }

    private static double[] RGB_XYZ(int red, int green, int blue) {
        double[] xyz = new double[3];

        double r = red / 255f;
        double g = green / 255f;
        double b = blue / 255f;

        //R
        if (r >= 0.04045)
            r = (float) Math.pow(((r + 0.055f) / 1.055f), 2.4f);
        else
            r /= 12.92f;

        //G
        if (g >= 0.04045)
            g = Math.pow(((g + 0.055f) / 1.055f), 2.4f);
        else
            g /= 12.92f;

        //B
        if (b >= 0.04045)
            b = Math.pow(((b + 0.055f) / 1.055f), 2.4f);
        else
            b /= 12.92f;

        r *= 100;
        g *= 100;
        b *= 100;

        double x = 0.412453f * r + 0.35758f * g + 0.180423f * b;
        double y = 0.212671f * r + 0.71516f * g + 0.072169f * b;
        double z = 0.019334f * r + 0.119193f * g + 0.950227f * b;

        xyz[0] = x;
        xyz[1] = y;
        xyz[2] = z;

        return xyz;
    }

    private static int[] XYZ_RGB(double X, double Y, double Z) {
        int[] rgb = new int[3];

        X /= 100;
        Y /= 100;
        Z /= 100;

        double r = 3.240479f * X - 1.53715f * Y - 0.498535f * Z;
        double g = -0.969256f * X + 1.875991f * Y + 0.041556f * Z;
        double b = 0.055648f * X - 0.204043f * Y + 1.057311f * Z;

        if (r >= 0.0031308)
            r = 1.055 * Math.pow(r, 0.4166f) - 0.055;
        else
            r = 12.92 * r;

        if (g >= 0.0031308)
            g = 1.055 * Math.pow(g, 0.4166) - 0.055;
        else
            g = 12.92 * g;

        if (b >= 0.0031308)
            b = 1.055 * Math.pow(b, 0.4166) - 0.055;
        else
            b = 12.92 * b;

        rgb[0] = (int) (r * 255);
        rgb[1] = (int) (g * 255);
        rgb[2] = (int) (b * 255);

        return rgb;
    }

    private static double[] XYZ_LAB(double X, double Y, double Z) {
        Function<Double, Double> function = x -> Math.pow(x, (double) 1 / 3) >= 0.008856 ? Math.pow(x, (double) 1 / 3) : (7.787 * x + 16 / 116);

        return new double[]{
                116 * function.apply(Y / 100) - 16,
                500 * (function.apply(X / 95.047) - function.apply(Y / 100)),
                200 * (function.apply(Y / 100) - function.apply(Z / 108.883))
        };
    }

    private static double[] LAB_XYZ(double L, double A, double B) {
        Function<Double, Double> function = x -> Math.pow(x, 3) >= 0.008856 ? Math.pow(x, 3) : ((x - 16 / 116) / 7.787);

        return new double[]{
                function.apply(A / 500 + (L + 16) / 116) * 100,
                function.apply((L + 16) / 116) * 95.047,
                function.apply((L + 16) / 116 - B / 200) * 108.883
        };
    }

    private static double[] RGB_HSV(int red, int green, int blue) {
        double[] hsv = new double[3];
        float r = red / 255f;
        float g = green / 255f;
        float b = blue / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        // Hue
        if (max == min) {
            hsv[0] = 0;
        } else if (max == r) {
            hsv[0] = ((g - b) / delta) * 60f;
        } else if (max == g) {
            hsv[0] = ((b - r) / delta + 2f) * 60f;
        } else if (max == b) {
            hsv[0] = ((r - g) / delta + 4f) * 60f;
        }

        // Saturation
        if (delta == 0)
            hsv[1] = 0;
        else
            hsv[1] = delta / max;

        //Value
        hsv[2] = max;

        return hsv;
    }

    private static int[] HSV_RGB(double hue, double saturation, double value) {
        int[] rgb = new int[3];

        float hi = (float) Math.floor(hue / 60.0) % 6;
        float f = (float) ((hue / 60.0) - Math.floor(hue / 60.0));
        float p = (float) (value * (1.0 - saturation));
        float q = (float) (value * (1.0 - (f * saturation)));
        float t = (float) (value * (1.0 - ((1.0 - f) * saturation)));

        if (hi == 0) {
            rgb[0] = (int) (value * 255);
            rgb[1] = (int) (t * 255);
            rgb[2] = (int) (p * 255);
        } else if (hi == 1) {
            rgb[0] = (int) (q * 255);
            rgb[1] = (int) (value * 255);
            rgb[2] = (int) (p * 255);
        } else if (hi == 2) {
            rgb[0] = (int) (p * 255);
            rgb[1] = (int) (value * 255);
            rgb[2] = (int) (t * 255);
        } else if (hi == 3) {
            rgb[0] = (int) (p * 255);
            rgb[1] = (int) (value * 255);
            rgb[2] = (int) (q * 255);
        } else if (hi == 4) {
            rgb[0] = (int) (t * 255);
            rgb[1] = (int) (value * 255);
            rgb[2] = (int) (p * 255);
        } else if (hi == 5) {
            rgb[0] = (int) (value * 255);
            rgb[1] = (int) (p * 255);
            rgb[2] = (int) (q * 255);
        }

        return rgb;
    }

    public static double[] CMYK_LAB(double c, double m, double y, double k) {
        int[] rgb = CMYK_RGB(c, m, y, k);
        double[] xyz = RGB_XYZ(rgb[0], rgb[1], rgb[2]);
        return XYZ_LAB(xyz[0], xyz[1], xyz[2]);
    }

    public static double[] LAB_HSV(double L, double A, double B) {
        double[] xyz = LAB_XYZ(L, A, B);
        int[] rgb = XYZ_RGB(xyz[0], xyz[1], xyz[2]);
        return RGB_HSV(rgb[0], rgb[1], rgb[2]);
    }

    public static double[] HSV_CMYK(double hue, double saturation, double value) {
        int[] rgb = HSV_RGB(hue, saturation, value);
        return RGB_CMYK(rgb[0], rgb[1], rgb[2]);
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(CMYK_RGB(1, 0.5, 0.2, 0.5)));
        System.out.println(Arrays.toString(RGB_CMYK(0, 63, 102)));
        System.out.println(Arrays.toString(RGB_XYZ(0, 63, 102)));
        System.out.println();
    }
}
