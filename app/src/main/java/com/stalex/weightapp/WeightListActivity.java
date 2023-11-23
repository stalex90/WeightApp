package com.stalex.weightapp;

import static com.patrykandpatrick.vico.core.entry.EntryListExtensionsKt.entryModelOf;
import static com.stalex.weightapp.Utils.getDayAddition;
import static com.stalex.weightapp.Utils.sortListByDate;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine;
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent;
import com.patrykandpatrick.vico.core.entry.ChartEntryModel;
import com.patrykandpatrick.vico.views.chart.ChartView;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WeightListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WeightViewAdapter adapter;
    private LinearLayoutManager layoutManager;
    private TextView currentWeightTextView;
    private TextView lastDateTextView;
    private TextView firstWeightTextView;
    private TextView desireWeightTextView;
    private ChartView chartView;
    private ArrayList<WeightItem> weightsList;
    private DatabaseReference allWeightsReference;
    private DatabaseReference desireWeightReference;
    private ChildEventListener weightsEventListener;
    private ChildEventListener desireWeightEventListener;
    private ActivityResultLauncher<Intent> editActivityResultLauncher;
    private FirebaseAuth auth;
    private String lastWeight;
    private String firstWeight;
    private String desireWeight;
    private String lastDate;
    private ChartEntryModel chartEntryModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_list);
        auth = FirebaseAuth.getInstance();

        setTitle("Текущий Вес");

        currentWeightTextView = findViewById(R.id.currentWeightTextView);
        lastDateTextView = findViewById(R.id.lastDateTextView);
        firstWeightTextView = findViewById(R.id.firstWeightTextView);
        desireWeightTextView = findViewById(R.id.desireWeightTextView);
        weightsList = new ArrayList<>();
        chartView = findViewById(R.id.chart_view);

        allWeightsReference = FirebaseDatabase.getInstance().getReference("weights").child(auth.getCurrentUser().getUid());

        editActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 3) {
                        Intent data = result.getData();
                        adapter.deleteMethod(allWeightsReference,
                                Integer.parseInt(data.getStringExtra("position")));
                    } else if (result.getResultCode() == 2) {
                        Intent data = result.getData();
                        adapter.changeMethod(allWeightsReference,
                                Integer.parseInt(data.getStringExtra("position")),
                                data.getStringExtra("weight"),
                                data.getStringExtra("date"));
                    }
                });

        attachDatabaseWeightsEventListener();
        attachDatabaseDesireWeightEventListener();
        buildRecyclerView();
        currentWeightTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddingWeightActivity.class);
            intent.putExtra("intent", "current");
            if (lastWeight != null) {
                intent.putExtra("weight", lastWeight);
            }
            startActivity(intent);
        });

        desireWeightTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddingWeightActivity.class);
            intent.putExtra("intent", "desire");
            if (desireWeight != null) {
                intent.putExtra("weight", desireWeight);
            }
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void attachDatabaseWeightsEventListener() {
        if (weightsEventListener == null) {
            weightsEventListener = new ChildEventListener() {
                @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    WeightItem weight = snapshot.getValue(WeightItem.class);
                    weight.setId(snapshot.getKey());
                    weightsList.add(weight);
                    sortListByDate(weightsList);
                    setFirstLastCurrentWeights();
                    drawGraphic();
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    WeightItem weight = snapshot.getValue(WeightItem.class);
                    String key = snapshot.getKey();
                    int index = -1;
                    for (int i = 0; i < weightsList.size(); i++) {
                        if (weightsList.get(i).getId().equals(key)) {
                            index = i;
                        }
                    }
                    weightsList.get(index).setWeightValue(weight.getWeightValue());
                    weightsList.get(index).setDate(weight.getDate());
                    sortListByDate(weightsList);
                    setFirstLastCurrentWeights();
                    drawGraphic();
                    buildRecyclerView();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String key = snapshot.getKey();
                    int index = -1;
                    for (int i = 0; i < weightsList.size(); i++) {
                        if (weightsList.get(i).getId().equals(key)) {
                            index = i;
                        }
                    }
                    weightsList.remove(index);
                    setFirstLastCurrentWeights();
                    drawGraphic();
                    buildRecyclerView();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            allWeightsReference.addChildEventListener(weightsEventListener);
        }
    }

    private void attachDatabaseDesireWeightEventListener() {
        desireWeightReference = FirebaseDatabase.getInstance().getReference("desireWeight").child(auth.getCurrentUser().getUid());
        if (desireWeightEventListener == null) {
            desireWeightEventListener = new ChildEventListener() {
                @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    WeightItem weight = snapshot.getValue(WeightItem.class);
                    desireWeight = weight.getWeightValue();
                    desireWeightTextView.setText(new BigDecimal(desireWeight).stripTrailingZeros().toPlainString() + " кг");
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            desireWeightReference.addChildEventListener(desireWeightEventListener);
        }
    }

    private void buildRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        adapter = new WeightViewAdapter(weightsList);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        adapter.setOnWeightClickListener(position -> {
            Intent intent = new Intent(WeightListActivity.this, EditWeightActivity.class);
            intent.putExtra("position", String.valueOf(position));
            intent.putExtra("weight", weightsList.get(position).getWeightValue());
            intent.putExtra("date", weightsList.get(position).getDate());
            editActivityResultLauncher.launch(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddingWeightActivity.class);
            intent.putExtra("intent", "current");
            if (lastWeight != null) {
                intent.putExtra("weight", lastWeight);
            }
            startActivity(intent);
        } else if (id == R.id.action_exit) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.weight_menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    public void setFirstLastCurrentWeights(){
        firstWeight = weightsList.get(0).getWeightValue();
        firstWeightTextView.setText(new BigDecimal(firstWeight).stripTrailingZeros().toPlainString() + " кг");
        lastWeight = weightsList.get(weightsList.size()-1).getWeightValue();
        lastDate = weightsList.get(weightsList.size()-1).getDate();
        currentWeightTextView.setText(new BigDecimal(lastWeight).stripTrailingZeros().toPlainString() + " кг");
        lastDateTextView.setText("Последнее взвешивание - " + getDayAddition(lastDate));
    }

    public void drawGraphic(){
        List<Number> list = weightsList.stream().map(item -> Double.parseDouble(item.getWeightValue())).collect(Collectors.toList());
        chartEntryModel = entryModelOf(list.toArray(new Number[list.size()]));
        chartView.setModel(chartEntryModel);
    }
}