package de.threateningcodecomments.routinetimer;

import java.util.ArrayList;

class Routine {

    private static final int MODE_CONTINUOUS = 1;
    private static final int MODE_SEQUENTIAL = 0;

    private int mode;

    private String name;
    private ArrayList<Tile> tiles;

    private String UID;

    //region Getters and Setters
    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(ArrayList<Tile> tiles) {
        this.tiles = tiles;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        if (this.UID == null) {
            this.UID = UID;
        }
    }
    //endregion
}
