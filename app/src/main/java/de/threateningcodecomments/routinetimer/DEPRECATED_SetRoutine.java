/*
package de.threateningcodecomments.routinetimer;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.maltaisn.icondialog.IconDialog;
import com.maltaisn.icondialog.IconDialogSettings;
import com.maltaisn.icondialog.data.Icon;
import com.maltaisn.icondialog.pack.IconPack;

import java.util.List;

public class DEPRECATED_SetRoutine extends AppCompatActivity implements IconDialog.Callback {

    private static final String ICON_DIALOG_TAG = "icon-dialog";

    private EditText nameField;
    private TextView nameView;
    private MaterialButton iconButton;
    private ImageView imageView;
    private MaterialCardView cardView;
    private MaterialButton colorButton;

    private Tile tmpTile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_routine);

        tmpTile = ResourceClass.getTmpTile();

        initBufferViews();

        initListeners();

        initBufferedVals();

        tmpTile.setAccessibility(ResourceClass.isNightMode(getApplication()));

        String[] COUNTRIES = new String[]{"Item 1", "Item 2", "Item 3", "Item 4", "edit"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.routine_popup_item,
                        COUNTRIES);

        AutoCompleteTextView editTextFilledExposedDropdown =
                findViewById(R.id.dd_SelectRoutine_createRoutine_mode);
        editTextFilledExposedDropdown.setAdapter(adapter);
    }

    private void initListeners() {
        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                if (!text.equals("")) {
                    setTileText(text);
                }
            }
        });

        IconDialog iconDialog = initIconSelecter();
        iconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconDialog.show(getSupportFragmentManager(), ICON_DIALOG_TAG);
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View customLayout = getLayoutInflater().inflate(R.layout.layout_colordialog, null);

                final View colorView = customLayout.findViewById(R.id.v_colordialog);

                Slider slider = customLayout.findViewById(R.id.sl_colordialog_color);
                slider.addOnChangeListener(new Slider.OnChangeListener() {
                    @Override
                    public void onValueChange(@NonNull Slider slider, float hue, boolean fromUser) {
                        handleColor(hue, colorView);
                    }
                });

                float hue = calculateSliderHue();
                handleColor(hue, colorView);
                slider.setValue(hue);

                final MaterialAlertDialogBuilder colordialog = new MaterialAlertDialogBuilder(DEPRECATED_SetRoutine.this)
                        .setTitle("Pick Color")
                        .setView(customLayout)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateCV();
                            }
                        })
                        .setCancelable(false);
                colordialog.show();
            }
        });
    }

    private float calculateSliderHue() {
        Tile tmpTile = ResourceClass.getTmpTile();
        int bgColor = tmpTile.getBackgroundColor();

        float[] hsv = new float[3];
        Color.colorToHSV(bgColor, hsv);

        int i = calculateColor(hsv[0]);
        Color.colorToHSV(i, hsv);

        return hsv[0];
    }

    private void handleColor(float hue, View colorView) {
        int color = calculateColor(hue);

        ResourceClass.getTmpTile().setBackgroundColor(color);
        colorView.setBackgroundColor(color);
    }

    private int calculateColor(float hue) {
        float[] hsv = {hue, 1.0F, 1.0F};
        int color = Color.HSVToColor(hsv);

        if (hue < 0.1) {
            color = Tile.DEFAULT_COLOR_DARK;
        }

        color = ResourceClass.convertColorDayNight(ResourceClass.isNightMode(getApplication()), color);
        return color;
    }

    private IconDialog initIconSelecter() {
        //handle setup of iconSelecter
        // If dialog is already added to fragment manager, get it. If not, create a new instance.
        IconDialog dialog = (IconDialog) getSupportFragmentManager().findFragmentByTag(ICON_DIALOG_TAG);
        final IconDialog iconDialog = dialog != null ? dialog
                : IconDialog.newInstance(new IconDialogSettings.Builder().build());
        Context context = this;
        ResourceClass.initIconPack(context);

        return iconDialog;
    }

    private void initBufferViews() {
        ResourceClass.getTmpTile().setAccessibility(ResourceClass.isNightMode(getApplication()));

        cardView = findViewById(R.id.cv_SetRoutine_main);
        imageView = findViewById(R.id.iv_SetRoutine_icon);
        nameView = findViewById(R.id.tv_SetRoutine_name);
        nameField = findViewById(R.id.et_SelectRoutine_createRoutine_name);
        iconButton = findViewById(R.id.btn_SetRoutine_icon);
        colorButton = findViewById(R.id.btn_SetRoutine_color);
    }

    private void updateCV() {
        Tile tmpTile = ResourceClass.getTmpTile();
        tmpTile.setAccessibility(ResourceClass.isNightMode(getApplication()));
        cardView.setCardBackgroundColor(tmpTile.getBackgroundColor());
        nameView.setTextColor(tmpTile.getContrastColor());
        imageView.setColorFilter(tmpTile.getContrastColor());
    }

    private void initBufferedVals() {
        Tile tmpTile = ResourceClass.getTmpTile();
        tmpTile.setAccessibility(ResourceClass.isNightMode(getApplication()));
        int contrastColor = tmpTile.getContrastColor();
        MyLog.d(contrastColor);

        String tempText = tmpTile.getName();
        if (!tempText.equals(Tile.DEFAULT_NAME)) {
            nameView.setText(tempText);
        }
        nameView.setTextColor(contrastColor);

        int iconID = tmpTile.getIconID();
        Drawable tempDraw = ResourceClass.getIconPack().getIcon(iconID).getDrawable();
        if (!tempDraw.equals(Tile.DEFAULT_ICONID)) {
            imageView.setImageDrawable(tempDraw);
        }
        imageView.setColorFilter(tmpTile.getContrastColor());

        int tempColor = tmpTile.getBackgroundColor();
        if (tempColor != Tile.DEFAULT_COLOR) {
            cardView.setCardBackgroundColor(tempColor);
        }
    }

    private void setTileText(String text) {
        nameView.setText(text);
        ResourceClass.getTmpTile().setName(text);
    }

    @Nullable
    @Override
    public IconPack getIconDialogIconPack() {
        return ResourceClass.getIconPack();
    }

    @Override
    public void onIconDialogIconsSelected(@NonNull IconDialog dialog, @NonNull List<Icon> icons) {
        Icon imageViewIcon = icons.get(0);

        tmpTile.setIconID(imageViewIcon.getId());
    }

    @Override
    public void onIconDialogCancelled() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}*/
