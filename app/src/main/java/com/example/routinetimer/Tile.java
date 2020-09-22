package com.example.routinetimer;

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
    public final static int DEFAULT_COLOR = 0xFFFFFF;

    public final static int LIGHT_THEME_TEXT_COLOR = Color.BLACK;
    public final static int DARK_THEME_TEXT_COLOR = Color.WHITE;

    private String name;

    private int contrastColor;

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
        setDayNightMode();
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        setDayNightMode();
        this.icon = icon;
    }

    public int getColor() {
        return bgColor;
    }

    public void setColor(int bgColor) {
        setDayNightMode();
        this.bgColor = bgColor;
    }

    public int getContrastColor() {
        return contrastColor;
    }

    public void setContrastColor(int contrastColor) {
        this.contrastColor = contrastColor;
    }

    public void setAccessibility(boolean isNightMode) {
        setDayNightMode(isNightMode);
        calculateContrast();
    }

    private void setAccessibility() {
        setDayNightMode(this.isNightMode);
        calculateContrast();
    }

    private void calculateContrast() {
        float[] hsvBackground = new float[3];
        Color.colorToHSV(bgColor, hsvBackground);

        MyLog.d(hsvBackground[0]);
        MyLog.d(hsvBackground[0] <= 80 || hsvBackground[0] >= 40);

        if (hsvBackground[0] <= 90 && hsvBackground[0] >= 40) {
            contrastColor = Color.BLACK;
        } else {
            contrastColor = Color.WHITE;
        }
    }

    private void setDayNightMode(boolean isNightMode) {
        this.isNightMode = isNightMode;

        ResourceClass.convertDrawableDayNight(isNightMode, icon);
        bgColor = ResourceClass.convertColorDayNight(isNightMode, bgColor);
    }

    private void setDayNightMode() {
        setDayNightMode(isNightMode);
    }
}
