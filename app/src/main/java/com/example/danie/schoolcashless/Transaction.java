package com.example.danie.schoolcashless;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Created by danie on 29/7/2016.
 */
public class Transaction {

    private String date, isoTime;
    private int storeId;
    private String from;
    private double price;

    public Transaction(String isoTime, String from, double price) {
        this.isoTime = isoTime;
        this.from = from;
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

    public String getIsoTime() {
        return isoTime;
    }

    public void setIsoTime(String isoTime) {
        this.isoTime = isoTime;
    }

    public String retrieveDate() {
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String str = fmt.print(dt);
        return str;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
