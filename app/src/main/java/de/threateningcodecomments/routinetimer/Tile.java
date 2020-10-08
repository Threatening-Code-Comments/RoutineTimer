package de.threateningcodecomments.routinetimer;

import android.graphics.Color;

class Tile {
    public static final Tile ERROR_TILE = new Tile(Tile.ERROR_NAME, Tile.ERROR_ICONID, Color.RED);

    public final static String DEFAULT_NAME = "";
    public final static String ERROR_NAME = "HELP THIS IS ERROR AAAH";

    public final static int DEFAULT_ICONID = 0;
    public final static int ERROR_ICONID = 69420;

    public final static int DEFAULT_COLOR = 0xFFFFFFFF;
    public final static int DEFAULT_COLOR_DARK = 0xFF242424;

    public final static int MODE_COUNT_UP = 1;
    public final static int MODE_COUNT_DOWN = -1;


    private String name;

    private int iconID;

    private int backgroundColor;
    private int contrastColor;

    private boolean isNightMode;

    private int mode;

    //region Constructors
    public Tile() {
        this(DEFAULT_NAME, DEFAULT_ICONID, DEFAULT_COLOR, DEFAULT_COLOR_DARK, false, MODE_COUNT_UP);
    }

    public Tile(String name, int iconID, int color) {
        this(name, iconID, color, ResourceClass.calculateContrast(color), false, MODE_COUNT_UP);
    }

    public Tile(String name, int iconID, int backgroundColor, int contrastColor, boolean isNightMode, int mode) {
        this.name = name;
        this.iconID = iconID;
        this.backgroundColor = backgroundColor;
        this.contrastColor = contrastColor;
        this.isNightMode = isNightMode;
        this.mode = mode;
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

    public void setIconWithID(int ID) {
        setAccessibility();

        this.iconID = ID;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int bgColor) {
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

    public boolean isNightMode() {
        return isNightMode;
    }

    public void setNightMode(boolean nightMode) {
        isNightMode = nightMode;
        setAccessibility();
    }

    //endregion
}
