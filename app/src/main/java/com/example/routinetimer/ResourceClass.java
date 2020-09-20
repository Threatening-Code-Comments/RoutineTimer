package com.example.routinetimer;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.maltaisn.icondialog.pack.IconPack;
import com.maltaisn.icondialog.pack.IconPackLoader;
import com.maltaisn.iconpack.defaultpack.IconPackDefault;

class ResourceClass {

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

    //------------------------------temp tile handling--------------------------------------------//

    private static IconPack loadIconPack() {
        // Create an icon pack loader with application context.
        IconPackLoader loader = new IconPackLoader(context);

        // Create an icon pack and load all drawables.
        iconPack = IconPackDefault.createDefaultIconPack(loader);
        iconPack.loadDrawables(loader.getDrawableLoader());

        return iconPack;
    }

    public static String getTmpTileName() {
        return tmpTile.getName();
    }

    public static void setTmpTileName(String name) {
        tmpTile.setName(name);
    }

    public static Drawable getTmpTileDrawable() {
        return tmpTile.getIcon();
    }

    public static void setTmpTileDrawable(Drawable d) {
        tmpTile.setIcon(d);
    }

    public static int getTmpTileColor() {
        return tmpTile.getColor();
    }

    public static void setTmpTileColor(int color) {
        tmpTile.setColor(color);
    }

    public static void resetTmpTile() {
        tmpTile = new Tile();
    }

    public static Tile getTmpTile() {
        return tmpTile;
    }
}
