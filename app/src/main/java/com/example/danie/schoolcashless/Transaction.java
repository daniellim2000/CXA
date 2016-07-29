package com.example.danie.schoolcashless;

/**
 * Created by danie on 29/7/2016.
 */
public class Transaction {

    private String date, store;
    private double price;

    public Transaction(String date, String store, double price) {
        this.date = date;
        this.store = store;
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
