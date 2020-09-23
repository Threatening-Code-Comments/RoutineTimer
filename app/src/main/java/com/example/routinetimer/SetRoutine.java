package com.example.routinetimer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.maltaisn.icondialog.IconDialog;
import com.maltaisn.icondialog.IconDialogSettings;
import com.maltaisn.icondialog.data.Icon;
import com.maltaisn.icondialog.pack.IconPack;

import java.util.List;

public class SetRoutine extends AppCompatActivity implements IconDialog.Callback {

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

        initViewBuffers();

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

        //handle setup of iconSelecter
        // If dialog is already added to fragment manager, get it. If not, create a new instance.
        IconDialog dialog = (IconDialog) getSupportFragmentManager().findFragmentByTag(ICON_DIALOG_TAG);
        final IconDialog iconDialog = dialog != null ? dialog
                : IconDialog.newInstance(new IconDialogSettings.Builder().build());
        Context context = this;
        ResourceClass.init(context);
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
                        float[] hsv = {hue, 1.0F, 1.0F};

                        int color = ResourceClass.convertColorDayNight(isNightMode(), Color.HSVToColor(hsv));

                        tmpTile.setColor(color);
                        colorView.setBackgroundColor(color);


                    }
                });

                final MaterialAlertDialogBuilder colordialog = new MaterialAlertDialogBuilder(SetRoutine.this)
                        .setTitle("Pick Color (Hue):")
                        .setView(customLayout)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateCV();
                            }
                        });
                colordialog.show();

            }
        });

        updateCV();
    }

    private void initViewBuffers() {
        tmpTile = ResourceClass.getTmpTile();
        cardView = findViewById(R.id.cv_SetRoutine_main);
        imageView = findViewById(R.id.iv_SetRoutine_icon);
        nameView = findViewById(R.id.tv_SetRoutine_name);
        nameField = findViewById(R.id.et_SetRoutine_name);
        iconButton = findViewById(R.id.btn_SetRoutine_icon);
        colorButton = findViewById(R.id.btn_SetRoutine_color);
    }

    private void updateCV() {
        tmpTile.setAccessibility();
        cardView.setCardBackgroundColor(tmpTile.getColor());
        nameView.setTextColor(tmpTile.getContrastColor());
        imageView.setImageDrawable(tmpTile.getIcon());
        imageView.setColorFilter(tmpTile.getContrastColor());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setTileText(String text) {
        nameView.setText(text);
        tmpTile.setName(text);
    }

    @Nullable
    @Override
    public IconPack getIconDialogIconPack() {
        return ResourceClass.getIconPack();
    }

    @Override
    public void onIconDialogIconsSelected(@NonNull IconDialog dialog, @NonNull List<Icon> icons) {
        Icon imageViewIcon = icons.get(0);

        Drawable unwrappedDrawable = imageViewIcon.getDrawable();

        imageView.setImageDrawable(imageViewIcon.getDrawable());

        tmpTile.setIcon(unwrappedDrawable);
    }

    @Override
    public void onIconDialogCancelled() {
    }

    private boolean isNightMode() {
        int nightModeFlags = getApplication().getResources().getConfiguration().uiMode &
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
}