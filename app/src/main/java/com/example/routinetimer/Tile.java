package com.example.routinetimer;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class Tile {
    public final static String DEFAULT_NAME = "";
    public final static Drawable DEFAULT_DRAWABLE = Drawable.createFromPath("src/main/res/drawable/ic_defaultdrawable.xml");
    public final static int DEFAULT_COLOR = 0xFFFFFF;

    public final static int LIGHT_THEME_TEXT_COLOR = Color.BLACK;
    public final static int DARK_THEME_TEXT_COLOR = Color.WHITE;

    private String name;

    private int contrastColor;

    private Drawable icon;
    private int bgColor = DEFAULT_COLOR;
    private boolean isNightMode;


    public Tile(String name, Drawable icon, int bgColor) {
        this.name = name;
        this.icon = icon;
        this.bgColor = bgColor;
    }

    public Tile() {
        this(DEFAULT_NAME, DEFAULT_DRAWABLE, DEFAULT_COLOR);
    }

    public void setAccessibility() {
        contrastColor = ResourceClass.calculateContrast(bgColor);
    }


    //-----------------------------getters and setters--------------------------------------------//

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public int getColor() {
        return bgColor;
    }

    public void setName(String name) {
        setAccessibility();
        this.name = name;
    }

    public void setIcon(Drawable icon) {
        setAccessibility();
        this.icon = icon;
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
}
