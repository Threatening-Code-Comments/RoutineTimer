package de.threateningcodecomments.routinetimer;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;

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
    static boolean listenerAdded = false;
    private static FirebaseDatabase database;


    //region random

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

    //region Routines
    private static ArrayList<Routine> routines = new ArrayList<>();

    public static boolean isNightMode(Application application) {
        int nightModeFlags = application.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;

            case Configuration.UI_MODE_NIGHT_NO:

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return false;
        }

        return false;
    }

    //endregion
    private static Tile tmpTile = new Tile();
    //region Icon Pack
    @Nullable
    private static IconPack iconPack;
    private static Context context;

    private static void handleRoutineUpdate(DataSnapshot snapshot) {
        Iterable<DataSnapshot> routinesIterable = snapshot.getChildren();

        routines.clear();
        //iterates through all routines
        for (DataSnapshot routineDataSnapshot : routinesIterable) {
            Routine routine = routineDataSnapshot.getValue(Routine.class);
            routines.add(routine);
        }
    }

    public static ArrayList<Routine> getRoutines() {
        return routines;
    }

    public static void setRoutines(ArrayList<Routine> routines) {
        ResourceClass.routines = routines;
    }
    //endregion

    //region tmpTile

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

    public static void resetTmpTile() {
        tmpTile = new Tile();
    }

    public static Tile getTmpTile() {
        return tmpTile;
    }
    //endregion

    public static void loadRoutines() {
        database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        DatabaseReference routineRef = database.getReference("/users/" + user.getUid() + "/routines/");

        routineRef.getParent().child("isListenedTo").getRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    snapshot.getRef().setValue(false);
                    listenerAdded = false;
                } else {
                    String bool = snapshot.getValue().toString();
                    listenerAdded = Boolean.getBoolean(bool);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!listenerAdded) {
            routineRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DatabaseReference isListenedToRef = database.getReference("/users/" + user.getUid() + "/isListenedTo");
                    isListenedToRef.setValue(true);

                    MyLog.d(snapshot.getValue() + " is the new value for routineRef");
                    handleRoutineUpdate(snapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public static void saveRoutine(Routine routine) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        DatabaseReference routinesRef = database.getReference("/users/" + user.getUid() + "/routines/");

        DatabaseReference child = routinesRef.child(routine.getUID());

        child.setValue(routine);
    }

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

    //endregion

}