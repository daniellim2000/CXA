package com.example.danie.schoolcashless;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danie.schoolcashless.model.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConfirmationActivity extends AppCompatActivity {

    String id;
    String otherPerson;
    Boolean youChargeOtherGuy;
    Boolean uAreFrom;
    double value;
    int intValue;

    GetTransaction mGetTransaction;

    UserSession userSession;

    TextView confirmReceive;
    TextView confirmPay;
    TextView confirmValue;
    TextView confirmFrom;
    TextView confirmTo;
    TextView confirmPerson;

    Button btnAccept;
    Button btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        userSession = UserSession.getInstance();

        confirmReceive = (TextView)findViewById(R.id.confirm_receive);
        confirmPay = (TextView)findViewById(R.id.confirm_pay);
        confirmValue= (TextView)findViewById(R.id.confirm_value);
        confirmFrom = (TextView)findViewById(R.id.confirm_from);
        confirmTo = (TextView)findViewById(R.id.confirm_to);
        confirmPerson = (TextView)findViewById(R.id.confirm_person);
        btnAccept = (Button)findViewById(R.id.btn_accept);
        btnDecline = (Button)findViewById(R.id.btn_decline);

        id = getIntent().getStringExtra("id");

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSession.transactionConfirmTo(id, false);
                finish();
            }
        });

        mGetTransaction = new GetTransaction(id);
        mGetTransaction.execute();


    }


    private void initialize(JSONObject success) {
        try {
            Log.d("Json", success.toString());

            JSONObject otherUser = success.getJSONObject("with");
            otherPerson = otherUser.getString("name");

            value = success.getDouble("value");

            JSONObject fromUser = success.getJSONObject("from");
            String fromName = fromUser.getString("name");
            JSONObject toUser = success.getJSONObject("to");
            String toName = toUser.getString("name");

            if (fromName.equalsIgnoreCase(otherPerson)) { //other initiated transaction
                confirmPerson.setText(fromName);
                uAreFrom = false;
                if (value < 0) { //other paying u
                    youChargeOtherGuy = true;
                } else { //u paying other
                    youChargeOtherGuy = false;
                }
            } else { //u initiated transaction
                confirmPerson.setText(toName);
                uAreFrom = true;
                if (value > 0) {
                    youChargeOtherGuy = true;
                } else {
                    youChargeOtherGuy = false;
                }
            }

            if (youChargeOtherGuy) {
                confirmReceive.setVisibility(View.VISIBLE);
                confirmFrom.setVisibility(View.VISIBLE);
            } else {
                confirmPay.setVisibility(View.VISIBLE);
                confirmTo.setVisibility(View.VISIBLE);
            }

            confirmValue.setText(String.valueOf(Math.abs(value)));

            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnAccept.setEnabled(false);

                    if (uAreFrom) {
                        userSession.transactionConfirmFrom(id, true);
                        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                        scheduler.scheduleAtFixedRate(new Runnable() {
                            public void run() {
                                if (userSession.transactionConfirmedTo(id)) {
                                    Toast.makeText(getApplicationContext(), "Transaction Complete!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }, 0, 200, TimeUnit.MILLISECONDS);

                    } else {
                        userSession.transactionConfirmTo(id, true);
                        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                        scheduler.scheduleAtFixedRate(new Runnable() {
                            public void run() {
                                if (userSession.transactionConfirmedFrom(id)) {
                                    Toast.makeText(getApplicationContext(), "Transaction Complete!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }, 0, 200, TimeUnit.MILLISECONDS);

                    }
                }
            });
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public class GetTransaction extends AsyncTask<Void, Void, JSONObject> {

        private final String id;

        GetTransaction(String id) {
            this.id = id;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                return userSession.getTransactionDetails(id);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error retrieving transaction data", Toast.LENGTH_LONG).show();
                finish();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject success) {
            mGetTransaction = null;
            if (success != null) {
                initialize(success);
            } else {
                Toast.makeText(getApplicationContext(), "Error processing transaction data", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mGetTransaction = null;
        }
    }
}
