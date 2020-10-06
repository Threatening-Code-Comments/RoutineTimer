package de.threateningcodecomments.routinetimer;

import java.util.ArrayList;
import java.util.UUID;

class Routine {

    public static final int MODE_CONTINUOUS = 1;
    public static final int MODE_SEQUENTIAL = 0;

    private int mode;

    private String name;
    private ArrayList<Tile> tiles;

    private String UID;

    //debug constructor
    public Routine(int mode, String name, ArrayList<Tile> tiles) {
        this.mode = mode;
        this.name = name;
        this.tiles = tiles;

        if (this.UID == null) {
            this.UID = UUID.randomUUID().toString();
        }
    }

    public Routine() {
    }

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
