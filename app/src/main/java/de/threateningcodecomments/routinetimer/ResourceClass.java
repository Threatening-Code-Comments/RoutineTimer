package de.threateningcodecomments.routinetimer;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.Nullable;

import com.maltaisn.icondialog.pack.IconPack;
import com.maltaisn.icondialog.pack.IconPackLoader;
import com.maltaisn.iconpack.defaultpack.IconPackDefault;

import java.util.ArrayList;
import java.util.HashMap;

class ResourceClass {

    public static final String DEFAULT_TMPROUTINENAME = "";
    private static String tmpRoutineName = DEFAULT_TMPROUTINENAME;
    //----------------------------------routine storage-------------------------------------------//
    private HashMap<String, ArrayList<Tile>> routineList = new HashMap<>();

    //----------------------------------------random----------------------------------------------//
    public static int convertColorDayNight(boolean isNightMode, int oldColor) {
        float[] hsvValues = new float[3];
        int red = Color.red(oldColor);
        int green = Color.green(oldColor);
        int blue = Color.blue(oldColor);
        Color.RGBToHSV(red, green, blue, hsvValues);

        if (hsvValues[0] == 0.0) {
            if (isNightMode) {
                return Tile.DEFAULT_COLOR_DARK;
            } else {
                return Tile.DEFAULT_COLOR;
            }
        }

        if (isNightMode) {
            hsvValues[1] = 0.5F;
        } else {
            hsvValues[1] = 1F;
        }

        return Color.HSVToColor(hsvValues);
    }

    //--------------------------------------tmp routine-------------------------------------------//

    public static int calculateContrast(int bgColor) {
        Color bgColor_cl = Color.valueOf(bgColor);
        double average = (bgColor_cl.red() + bgColor_cl.blue() + bgColor_cl.green()) / 3;

        int contrastColor;

        if (average > 0.5) {
            contrastColor = Color.BLACK;
        } else {
            contrastColor = Color.WHITE;
        }

        return contrastColor;
    }

    public static String getTmpRoutineName() {
        return tmpRoutineName;
    }

    public static void setTmpRoutineName(String tmpRoutineName) {
        ResourceClass.tmpRoutineName = tmpRoutineName;
    }

    public HashMap<String, ArrayList<Tile>> getRoutineList() {
        return routineList;
    }

    //--------------------------------------icon handling-----------------------------------------//
    @Nullable
    private static IconPack iconPack;
    private static Context context;
    private static Tile tmpTile = new Tile();

    public static void init(Context c) {
        context = c;

        // Load the icon pack on application start.
        loadIconPack();
    }

    @Nullable
    public static IconPack getIconPack() {
        return iconPack != null ? iconPack : loadIconPack();
    }

    private static IconPack loadIconPack() {
        // Create an icon pack loader with application context.
        IconPackLoader loader = new IconPackLoader(context);

        // Create an icon pack and load all drawables.
        iconPack = IconPackDefault.createDefaultIconPack(loader);
        iconPack.loadDrawables(loader.getDrawableLoader());

        return iconPack;
    }

    //------------------------------temp tile handling--------------------------------------------//

    public static void resetTmpTile() {
        tmpTile = new Tile();
    }

    public static Tile getTmpTile() {
        return tmpTile;
    }
}