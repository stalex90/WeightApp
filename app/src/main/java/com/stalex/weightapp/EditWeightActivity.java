package com.stalex.weightapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class EditWeightActivity extends AppCompatActivity {
    private Button deleteButton;
    private Button changeButton;
    private TextView toolbarText;
    private FirebaseDatabase database;
    private DatabaseReference weightsReference;
    private FirebaseAuth auth;
    private BigDecimal currentWeight;
    private Intent intent;
    private String position;
    private String date;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_weight);

        deleteButton = findViewById(R.id.deleteButton);
        changeButton = findViewById(R.id.saveButton);
        toolbarText = findViewById(R.id.toolbarText);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intent = getIntent();
        if (intent != null) {
            position = intent.getStringExtra("position");
            date = intent.getStringExtra("date");
            toolbarText.setText(date);
        }


        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        weightsReference = database.getReference("weights").child(auth.getCurrentUser().getUid());

        deleteButton.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("position", position);
            setResult(3, returnIntent);
            finish();
        });

        changeButton.setOnClickListener(v -> {
            String currentDate;
            if (date != null) {
                currentDate = date;
            } else {
                currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra("position", position);
            returnIntent.putExtra("weight", String.valueOf(currentWeight));
            returnIntent.putExtra("date", currentDate);
            setResult(2, returnIntent);
            finish();
        });

        toolbarText.setOnClickListener(v -> setDate());

        getSupportFragmentManager().setFragmentResultListener("currentWeight", this, (requestKey, bundle) -> {
            currentWeight = new BigDecimal(bundle.getString("bundleKey"));
        });
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
        Date myLastdate = null;
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
        try {
            myLastdate = fmt.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        AtomicReference<String> myDate = new AtomicReference<>();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditWeightActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        CalendarView dialogView = (CalendarView) inflater.inflate(R.layout.my_calendar, null);
        dialogView.setMaxDate(System.currentTimeMillis());
        dialogView.setOnDateChangeListener((view, year, month, dayOfMonth) -> myDate.set(dayOfMonth + "-" + (month + 1) + "-" + year));
        dialogView.setDate(myLastdate.getTime());
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (myDate.get() == null && date == null) {
                    toolbarText.setText("Сегодня");
                } else if (myDate.get() != null) {
                    date = myDate.get();
                    toolbarText.setText(date);
                } else {
                    toolbarText.setText(date);
                }
            }
        });
        dialogBuilder.setNegativeButton("Отменить", null);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }
}