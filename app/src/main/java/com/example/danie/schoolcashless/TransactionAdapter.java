package com.example.danie.schoolcashless;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by danie on 29/7/2016.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> list;

    public TransactionAdapter(List<Transaction> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction transaction = list.get(position);
        DecimalFormat df = new DecimalFormat("#.00");
        holder.mDateView.setText(transaction.retrieveDate());
        String type = "Paid";
        holder.mTitleView.setText(format(transaction.isPaid(), transaction.getPrice()) + transaction.getWith());
        //holder.mStore.setText(transaction.retrieveStoreName());
    }

    private String format(boolean paid, double value) {
        if (paid) {
            return "Paid $" + value + " to ";
        } else {
            return "Received $" + value + " from ";
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mDateView, mTitleView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView) itemView.findViewById(R.id.transaction_title);
            mDateView = (TextView) itemView.findViewById(R.id.transaction_date);
        }
    }
}
