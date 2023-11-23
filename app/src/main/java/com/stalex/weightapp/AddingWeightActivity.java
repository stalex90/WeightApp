package com.stalex.weightapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentResultListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class AddingWeightActivity extends AppCompatActivity {
    private Button saveButton;
    private TextView toolbarText;
    private FirebaseDatabase database;
    private DatabaseReference weightsReference;
    private DatabaseReference desireWeightReference;
    private FirebaseAuth auth;
    private Intent intent;
    private String date;
    private BigDecimal currentWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_weight);

        saveButton = findViewById(R.id.saveButton);
        toolbarText = findViewById(R.id.toolbarText);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        intent = getIntent();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        weightsReference = database.getReference("weights").child(auth.getCurrentUser().getUid());
        desireWeightReference = database.getReference("desireWeight").child(auth.getCurrentUser().getUid());


        saveButton.setOnClickListener(v -> saveWeight());
        toolbarText.setOnClickListener(v -> setDate());

        getSupportFragmentManager().setFragmentResultListener("currentWeight", this, (requestKey, bundle) -> {
            currentWeight = new BigDecimal(bundle.getString("bundleKey"));
        });
    }

    private void saveWeight() {
        String currentDate;
        if (date != null) {
            currentDate = date;
        } else {
            currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        }

        if (intent.getStringExtra("intent").equals("current")) {
            weightsReference.push().setValue(new WeightItem(currentDate, currentWeight.stripTrailingZeros().toPlainString()));
        } else if (intent.getStringExtra("intent").equals("desire")) {
            desireWeightReference.push().setValue(new WeightItem(currentDate, currentWeight.stripTrailingZeros().toPlainString()));
        }
        startActivity(new Intent(AddingWeightActivity.this, WeightListActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void setDate() {
        AtomicReference<String> myDate = new AtomicReference<>();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AddingWeightActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        CalendarView dialogView = (CalendarView) inflater.inflate(R.layout.my_calendar, null);
        dialogView.setMaxDate(System.currentTimeMillis());
        dialogView.setOnDateChangeListener((view, year, month, dayOfMonth) -> myDate.set(dayOfMonth + "-" + (month + 1) + "-" + year));
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (myDate.get() == null) {
                    toolbarText.setText("Сегодня");
                } else {
                    date = myDate.get();
                    toolbarText.setText(date);
                }
            }
        });
        dialogBuilder.setNegativeButton("Отменить", null);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }
}