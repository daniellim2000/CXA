package com.example.danie.schoolcashless;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.danie.schoolcashless.model.UserSession;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QRCodeActivity extends AppCompatActivity {

    ImageView imageView;
    UserSession userSession;
    JSONObject json;
    String id;
    CreateTransaction mCreateTask;
    ScheduledExecutorService scheduler;

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

        mCreateTask = new CreateTransaction(isCharge, value);
        mCreateTask.execute((Void) null);

    }

    @Override
    protected void onStart() {
        super.onStart();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if(userSession.getTransactionScanned(id)) {
                    Log.d("successscan", "Transaction was Scanned successfully");
                    startConfirmationActivity(id);
                    scheduler.shutdown();
                }
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }


    @Override
    protected void onStop() {
        super.onStop();

        scheduler.shutdown();
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

    private void startConfirmationActivity(String id) {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
        finish();
    }

    public class CreateTransaction extends AsyncTask<Void, Void, JSONObject> {

        private final Boolean isCharge;
        private final double value;

        CreateTransaction(Boolean isCharge, double value) {
            this.isCharge = isCharge;
            this.value = value;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            try {
                if(isCharge) {
                    json = userSession.createReceiveTransaction(value);
                } else {
                    json = userSession.createSendTransaction(value);
                }
                id = json.getString("_id");
            } catch(Exception e) {
                e.printStackTrace();
                json = null;
            }

            return json;
        }

        @Override
        protected void onPostExecute(final JSONObject success) {
            mCreateTask = null;
            if (success != null) {
                try {
                    Bitmap bitmap = generateQRCode(json.getString("code"));
                    imageView.setImageBitmap(bitmap);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (com.google.zxing.WriterException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Unable to create QR Code", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mCreateTask = null;
        }
    }
}
