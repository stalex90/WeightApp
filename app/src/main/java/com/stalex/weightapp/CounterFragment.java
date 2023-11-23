package com.stalex.weightapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CounterFragment extends Fragment {
    private EditText weightEditText;
    private Button minusButton;
    private Button plusButton;
    private Button saveButton;
    private BigDecimal currentWeight;
    private Intent intent;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        saveButton = getActivity().findViewById(R.id.saveButton);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        weightEditText = view.findViewById(R.id.weightEditText);
        minusButton = view.findViewById(R.id.minusButton);
        plusButton = view.findViewById(R.id.plusButton);
        intent = getActivity().getIntent();
        if (intent != null && intent.getExtras().size() > 1) {
            currentWeight = new BigDecimal(intent.getStringExtra("weight"));
            weightEditText.setText(currentWeight.stripTrailingZeros().toPlainString() + " кг");
        }

        if (weightEditText.getText().toString().trim().equals("")) {
            saveButton.setEnabled(false);
        }

        minusButton.setOnClickListener(v -> decreaseCount());
        plusButton.setOnClickListener(v -> increaseCount());

        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!weightEditText.getText().toString().trim().equals("")) {
                    saveButton.setEnabled(true);
                    currentWeight = new BigDecimal(weightEditText.getText().toString().replace("кг", "").trim());
                    Bundle result = new Bundle();
                    result.putString("bundleKey", currentWeight + "");
                    getParentFragmentManager().setFragmentResult("currentWeight", result);
                } else {
                    saveButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Bundle result = new Bundle();
        result.putString("bundleKey", currentWeight + "");
        getParentFragmentManager().setFragmentResult("currentWeight", result);

        // Inflate the layout for this fragment
        return view;
    }

    private void increaseCount() {
        if (currentWeight == null) {
            currentWeight = new BigDecimal("0");
        }
        currentWeight = currentWeight.add(new BigDecimal("0.1"));
        Bundle result = new Bundle();
        result.putString("bundleKey", currentWeight + "");
        getParentFragmentManager().setFragmentResult("currentWeight", result);
        weightEditText.setText(currentWeight.stripTrailingZeros().toPlainString() + " кг");
    }

    private void decreaseCount() {
        if (currentWeight == null) {
            currentWeight = new BigDecimal("0");
        }
        currentWeight = currentWeight.add(new BigDecimal("-0.1"));
        {
            if (currentWeight.compareTo(new BigDecimal("0")) < 0) {
                currentWeight = new BigDecimal("0");
            }
        }
        Bundle result = new Bundle();
        result.putString("bundleKey", currentWeight + "");
        getParentFragmentManager().setFragmentResult("currentWeight", result);
        weightEditText.setText(currentWeight.stripTrailingZeros().toPlainString() + " кг");
    }
}