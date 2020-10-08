package de.threateningcodecomments.routinetimer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

import static de.threateningcodecomments.routinetimer.R.id.ctbl_SelectRoutine_collapsingToolbarLayout;

public class SelectRoutine extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private ExtendedFloatingActionButton fab;
    private CollapsingToolbarLayout toolBarLayout;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Routine> routines;

    private Routine bufferRoutine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_routine);

        initBufferViews();

        initListeners();

        initRoutines();

        setSupportActionBar(toolbar);

        initRecyclerView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_SelectRoutine_add:
                bufferRoutine = ResourceClass.generateRandomRoutine();

                ResourceClass.saveRoutine(bufferRoutine);

                updateUI();
                break;
            default:
                Toast.makeText(this, "Wrong onClickListener ID. It seems you fucked up monumentally.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateUI() {
        if (routines.get(0) == Routine.ERROR_ROUTINE) {
            routines.clear();
        }

        routines.add(bufferRoutine);

        mAdapter.notifyDataSetChanged();
    }

    private void initListeners() {
        fab.setOnClickListener(this);
    }

    private void initBufferViews() {
        recyclerView = findViewById(R.id.rv_SelectRoutine_recyclerView);
        toolbar = (Toolbar) findViewById(R.id.tb_SelectRoutine_toolbar);
        toolBarLayout = findViewById(ctbl_SelectRoutine_collapsingToolbarLayout);
        fab = findViewById(R.id.fab_SelectRoutine_add);
    }

    private void initRoutines() {
        ResourceClass.loadRoutines();
        routines = ResourceClass.getRoutines();
    }

    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(routines);
        recyclerView.setAdapter(mAdapter);
    }
}