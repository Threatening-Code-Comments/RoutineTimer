package de.threateningcodecomments.routinetimer;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class SelectRoutine extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<Routine> routines;

    private MaterialTextView routineDisplay;
    private AutoCompleteTextView dropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_routine);

        ResourceClass.loadRoutines();

        initBufferViews();

        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        routines = ResourceClass.getRoutines();

        updateUI();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.dd_SelectRoutine_selectRoutine_dropdown:
                updateUI();
                break;

            default:
                Toast.makeText(this, "unknown Error. Please see developer or priest.", Toast.LENGTH_LONG).show();
        }
    }

    private void initBufferViews() {
        routineDisplay = findViewById(R.id.tv_SelectRoutine_selectRoutine_info);
        dropdown = findViewById(R.id.dd_SelectRoutine_selectRoutine_dropdown);
    }

    private void initListeners() {
        dropdown.setOnClickListener(this);
    }


    private void updateUI() {
        String[] routineNames = new String[routines.size()];

        for (int i = 0; i < routines.size(); i++) {
            routineNames[i] = routines.get(i).getName();
        }

        if (routineNames.length == 0) {
            routineNames = new String[1];
            routineNames[0] = "nothing here yet!";
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.routine_popup_item,
                        routineNames);

        dropdown.setAdapter(adapter);
    }
}