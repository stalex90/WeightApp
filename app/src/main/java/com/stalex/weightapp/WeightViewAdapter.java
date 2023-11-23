package com.stalex.weightapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;

public class WeightViewAdapter extends RecyclerView.Adapter<WeightViewAdapter.WightViewViewHolder> {

    private ArrayList<WeightItem> arrayList;
    private OnWeightClickListener listener;

    public interface OnWeightClickListener {
        void onWeightClick(int position);
    }

    public void setOnWeightClickListener(OnWeightClickListener listener) {
        this.listener = listener;
    }

    public WeightViewAdapter(ArrayList<WeightItem> arrayList) {
        this.arrayList = arrayList;
    }

    public class WightViewViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView weightTextView;
        public ImageView imageView;

        public WightViewViewHolder(@NonNull View itemView, OnWeightClickListener listener) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            weightTextView = itemView.findViewById(R.id.weightTextView);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onWeightClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public WightViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weight_item, parent, false);
        WightViewViewHolder wightViewViewHolder = new WightViewViewHolder(view, listener);
        return wightViewViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WightViewViewHolder holder, int position) {
        WeightItem weightItem = arrayList.get(position);
        holder.dateTextView.setText(weightItem.getDate());
        holder.weightTextView.setText(weightItem.getWeightValue() + " кг");
        if (position == 0) {
            holder.imageView.setImageResource(R.drawable.baseline_arrow_downward_24);
        } else if (Double.parseDouble(weightItem.getWeightValue()) > Double.parseDouble(arrayList.get(position-1).getWeightValue())){
            holder.imageView.setImageResource(R.drawable.baseline_arrow_upward_24);
        } else {
            holder.imageView.setImageResource(R.drawable.baseline_arrow_downward_24);
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void deleteMethod(DatabaseReference reference, int position) {
        reference.child(arrayList.get(position).getId()).removeValue();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeMethod(DatabaseReference reference, int position, String weight, String date) {
        reference.child(arrayList.get(position).getId()).child("weightValue").setValue(weight);
        reference.child(arrayList.get(position).getId()).child("date").setValue(date);
        notifyDataSetChanged();
    }
}
