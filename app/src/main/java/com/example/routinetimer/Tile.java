package com.example.routinetimer;

import android.graphics.Canvas;
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

        @Override
        public int getOpacity() {
            return 0;
        }
    };
    public final static int DEFAULT_COLOR = 0xFFFFFF;

    private String name;
    private Drawable icon;
    private int bgColor;

    private boolean isNightMode;


    public Tile(String name, Drawable icon, int bgColor) {
        this.name = name;
        this.icon = icon;
        this.bgColor = bgColor;
    }

    public Tile() {
        this(DEFAULT_NAME, DEFAULT_DRAWABLE, DEFAULT_COLOR);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getColor() {
        return bgColor;
    }

    public void setColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setDayNightMode(boolean isNightMode) {
        this.isNightMode = isNightMode;

        ResourceClass.convertDrawableDayNight(isNightMode, icon);
        bgColor = ResourceClass.convertColorDayNight(isNightMode, bgColor);
    }

    public void setDayNightMode() {
        setDayNightMode(isNightMode);
    }
}
