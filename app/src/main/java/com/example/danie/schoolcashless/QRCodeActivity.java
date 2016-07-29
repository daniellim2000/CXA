package com.example.danie.schoolcashless;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.example.danie.schoolcashless.model.UserSession;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONObject;

public class QRCodeActivity extends AppCompatActivity {

    ImageView imageView;
    UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        imageView = (ImageView)findViewById(R.id.qrcode);
        userSession = UserSession.getInstance();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        imageView.getLayoutParams().height = (int)(metrics.heightPixels * 0.7);

        Bundle extras = getIntent().getExtras();
        double value = Double.parseDouble(extras.getString("value"));
        Boolean isCharge = extras.getBoolean("isCharge");

        JSONObject json;

        try {
            if(isCharge) {
                json = userSession.createSendTransaction(value);
            } else {
                json = userSession.createReceiveTransaction(value);
            }
        } catch(Exception e) {
            e.printStackTrace();
            json = null;
            System.exit(3);
        }

        try {
            Bitmap bitmap = generateQRCode(json.getString("string"));
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap generateQRCode(String data)throws WriterException {
        com.google.zxing.Writer writer = new QRCodeWriter();
        String finaldata = Uri.encode(data, "utf-8");

        BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE, 150, 150);
        Bitmap imageBitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < 150; i++) {//width
            for (int j = 0; j < 150; j++) {//height
                imageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }

        return imageBitmap;
    }
}
