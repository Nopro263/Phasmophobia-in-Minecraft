package at.nopro.phasmo.lightingv2;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.palette.Palette;

import java.util.Set;

public class LightInterceptor {
    static Set<Point> calculateInternal(Light light, Palette var1, int var2, int var3, int var4, int[] var5, int var6, Light.LightLookup var7) {
        return light.calculateInternal(var1, var2, var3, var4, var5, var6, var7);
    }

    static Set<Point> calculateExternal(Light light, Palette var1, Point[] var2, Light.LightLookup var3, Light.PaletteLookup var4) {
        return light.calculateExternal(var1, var2, var3, var4);
    }
}
