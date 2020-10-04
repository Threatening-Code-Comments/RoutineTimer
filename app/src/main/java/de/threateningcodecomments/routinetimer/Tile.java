package de.threateningcodecomments.routinetimer;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class Tile {
    public final static String DEFAULT_NAME = "";
    public final static Drawable DEFAULT_DRAWABLE = new Drawable() {
        @Override
        public void draw(@NonNull Canvas canvas) {

        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @SuppressLint("WrongConstant")
        @Override
        public int getOpacity() {
            return 0;
        }
    };
    public final static int DEFAULT_COLOR = 0xFFFFFFFF;
    public final static int DEFAULT_COLOR_DARK = 0xFF242424;

    public final static int LIGHT_THEME_TEXT_COLOR = Color.BLACK;
    public final static int DARK_THEME_TEXT_COLOR = Color.WHITE;

    private String name;

    private Drawable icon;
    private int iconID;

    private int contrastColor;
    private int bgColor;

    private boolean isNightMode;


    public void setAccessibility(boolean isNightMode) {
        setDayNightMode(isNightMode);
        contrastColor = ResourceClass.calculateContrast(bgColor);
        icon.setTint(contrastColor);
    }

    private void setAccessibility() {
        setAccessibility(this.isNightMode);
    }

    private void setDayNightMode(boolean isNightMode) {
        this.isNightMode = isNightMode;

        bgColor = ResourceClass.convertColorDayNight(isNightMode, bgColor);
    }

    private void setDayNightMode() {
        setDayNightMode(isNightMode);
    }

    //--------------------------------------constructor-------------------------------------------//

    public Tile(String name, Drawable icon, int bgColor) {
        this.name = name;
        this.icon = icon;
        this.bgColor = bgColor;
    }

    public Tile() {
        this(DEFAULT_NAME, DEFAULT_DRAWABLE, DEFAULT_COLOR);
    }

    //--------------------------------------getter and setter-------------------------------------//

    public String getName() {
        return name;
    }

    public void setName(String name) {
        setDayNightMode();
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        setAccessibility();
        this.icon = icon;
    }

    public int getColor() {
        return bgColor;
    }

    public void setColor(int bgColor) {
        setAccessibility();
        this.bgColor = bgColor;
    }

    public int getContrastColor() {
        return contrastColor;
    }

    public void setContrastColor(int contrastColor) {
        this.contrastColor = contrastColor;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }
}
