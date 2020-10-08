package de.threateningcodecomments.routinetimer;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.maltaisn.icondialog.pack.IconPack;
import com.maltaisn.icondialog.pack.IconPackLoader;
import com.maltaisn.iconpack.defaultpack.IconPackDefault;

import java.util.ArrayList;

class ResourceClass {

    //region random
    private static Drawable errorDrawable;

    public static Drawable getErrorDrawable() {
        if (errorDrawable == null) {
            MyLog.d("LMAO THE ERROR DRAWABLE IS NULL");
            return new Drawable() {
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
                    return PixelFormat.OPAQUE;
                }
            };
        }
        return errorDrawable;
    }

    public static void setErrorDrawable(Drawable errorDrawable) {
        ResourceClass.errorDrawable = errorDrawable;
    }

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

    public static boolean isNightMode(Application application) {
        int nightModeFlags = application.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                updateNightMode(true);
                return true;

            case Configuration.UI_MODE_NIGHT_NO:

            case Configuration.UI_MODE_NIGHT_UNDEFINED:

            default:
                updateNightMode(false);
                return false;
        }
    }

    private static boolean isNightMode;

    public static boolean wasNightMode() {
        return isNightMode;
    }

    public static void updateNightMode(boolean nightMode) {
        isNightMode = nightMode;
    }

    public static void updateNightMode(Application application) {
        updateNightMode(isNightMode(application));
    }

    public static int calculateContrast(int bgColor) {
        Color bgColor_cl = Color.valueOf(bgColor);
        double average = (bgColor_cl.red() + bgColor_cl.blue() + bgColor_cl.green()) / 3;

        int contrastColor;

        if (average > 0.6) {
            contrastColor = Color.BLACK;
        } else {
            contrastColor = Color.WHITE;
        }

        return contrastColor;
    }

    public static int random(int start, int end) {

        double randomVal = Math.random();

        return (int) ((randomVal + start) * end);
    }

    //endregion

    //region Routines
    static boolean listenerAdded = false;
    private static FirebaseDatabase database;

    private static FirebaseUser lastUser;
    private static ValueEventListener valueEventListener;

    private static ArrayList<Routine> routines = new ArrayList<>();

    public static void loadRoutines() {
        database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            routines = new ArrayList<>();
            MyLog.d("FUCK FUCK FUCK ROUTINES IS NULL BECUASE OF NO USER HELP");
            return;
        }

        if (user != lastUser) {
            lastUser = user;

            DatabaseReference routineRef = database.getReference("/users/" + user.getUid() + "/routines/");

            MyLog.d(routineRef + " is the raw value of routineRef");

            if (valueEventListener != null) {
                routineRef.removeEventListener(valueEventListener);
                MyLog.d("old listener is being removed!");
            }

            MyLog.d("new listener is being added!");
            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    handleRoutineUpdate(snapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };

            routineRef.addValueEventListener(valueEventListener);

        }

    }

    private static void handleRoutineUpdate(DataSnapshot snapshot) {
        Iterable<DataSnapshot> routinesIterable = snapshot.getChildren();
        MyLog.d("new value for routines in db: " + snapshot.getValue());

        if (snapshot.getValue() == null) {
            routines.clear();
            routines.add(Routine.ERROR_ROUTINE);
            return;
        }

        routines.clear();
        //iterates through all routines
        for (DataSnapshot routineDataSnapshot : routinesIterable) {
            Routine routine = routineDataSnapshot.getValue(Routine.class);
            routines.add(routine);
        }
    }

    public static ArrayList<Routine> getRoutines() {
        return routines == null ? new ArrayList<Routine>() : routines;
    }

    public static void setRoutines(ArrayList<Routine> routines) {
        if (routines == null) {
            ResourceClass.routines = new ArrayList<>();
            return;
        }

        ResourceClass.routines = routines;
    }

    public static void saveRoutine(Routine routine) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        String path = "/users/" + user.getUid() + "/routines/";
        String key = routine.getUID();
        Object value = routine;

        saveToDb(path, key, value);
    }

    public static Routine generateRandomRoutine() {
        ArrayList<Tile> tiles = new ArrayList<>();
        for (int i = 0; i < (int) ((Math.random() + 1) * 3); i++) {
            tiles.add(new Tile("random tile name " + ResourceClass.random(0, 5) + "!",
                    ResourceClass.random(0, 80),
                    Color.rgb((float) Math.random() - 0.2F, (float) Math.random(), (float) Math.random())
            ));
        }

        return new Routine(Routine.MODE_SEQUENTIAL, "Random routine " + ResourceClass.random(0, 100), tiles);
    }

    //endregion

    //region Database handling

    public static void saveToDb(String path, String key, Object value) {

        DatabaseReference pathRef = database.getReference(path);

        DatabaseReference child = pathRef.child(key);

        child.setValue(value);
    }

    //endregion

    //region tmpTile
    private static Tile tmpTile = new Tile();

    public static void resetTmpTile() {
        tmpTile = new Tile();
    }

    public static Tile getTmpTile() {
        return tmpTile;
    }
    //endregion

    //region Icon Pack
    @Nullable
    private static IconPack iconPack;
    private static Context context;

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

    public static void initIconPack(Context c) {
        context = c;

        // Load the icon pack on application start.
        loadIconPack();
    }

    public static Drawable getIconDrawable(Tile tile) {
        int iconID = tile.getIconID();
        Drawable icon;
        if (iconID != Tile.ERROR_ICONID) {
            icon = ResourceClass.getIconPack().getIcon(iconID).getDrawable();
        } else {
            icon = ResourceClass.getErrorDrawable();
        }

        return icon;
    }
    //endregion

}