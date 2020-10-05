package de.threateningcodecomments.routinetimer;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.maltaisn.icondialog.data.Icon;

import java.util.Objects;

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

    public final static int MODE_COUNT_UP = 1;
    public final static int MODE_COUNT_DOWN = -1;

    private String name;

    private Drawable icon;
    private int iconID;

    private int contrastColor;
    private int backgroundColor;

    private boolean isNightMode;

    private int mode;

    //region Constructors
    public Tile(String name, Drawable icon, int backgroundColor) {
        this.name = name;
        this.icon = icon;
        this.backgroundColor = backgroundColor;
    }

    public Tile() {
        this(DEFAULT_NAME, DEFAULT_DRAWABLE, DEFAULT_COLOR);
    }
    //endregion

    private void setAccessibility() {
        setAccessibility(this.isNightMode);
    }

    private void setDayNightMode(boolean isNightMode) {
        this.isNightMode = isNightMode;

        backgroundColor = ResourceClass.convertColorDayNight(isNightMode, backgroundColor);
    }

    private void setDayNightMode() {
        setDayNightMode(isNightMode);
    }

    public void setAccessibility(boolean isNightMode) {
        setDayNightMode(isNightMode);
        contrastColor = ResourceClass.calculateContrast(backgroundColor);
        icon.setTint(contrastColor);
    }

    //region getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        setDayNightMode();
        this.name = name;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        setAccessibility();
        this.icon = icon;
    }

    public void setIconWithID(int ID) {
        setAccessibility();

        Icon icon = Objects.requireNonNull(ResourceClass.getIconPack()).getIcon(ID);

        this.iconID = ID;
        this.icon = Objects.requireNonNull(icon).getDrawable();
    }

    public int getColor() {
        return backgroundColor;
    }

    public void setColor(int bgColor) {
        setAccessibility();
        this.backgroundColor = bgColor;
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
    //endregion
}
