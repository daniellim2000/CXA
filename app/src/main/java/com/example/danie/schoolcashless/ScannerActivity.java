package com.example.danie.schoolcashless;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import net.danlew.android.joda.JodaTimeAndroid;

public class ScannerActivity extends CaptureActivity {

    DecoratedBarcodeView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        scannerView.getLayoutParams().height = (int)(metrics.heightPixels * 0.7);

        JodaTimeAndroid.init(this);
    }

    @Override
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.activity_scanner);
        return (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);
    }

}
