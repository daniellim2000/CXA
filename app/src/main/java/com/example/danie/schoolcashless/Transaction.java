package com.example.danie.schoolcashless;

/**
 * Created by danie on 29/7/2016.
 */
public class Transaction {

    private String date;
    private int storeId;
    private double price;

    public Transaction(String date, int storeId, double price) {
        this.date = date;
        this.storeId = storeId;
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String retrieveStoreName() {
        String storeName = "";
        return storeName;
    }
}
