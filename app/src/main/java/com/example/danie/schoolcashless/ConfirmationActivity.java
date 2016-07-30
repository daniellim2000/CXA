package com.example.danie.schoolcashless;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private String id;
    private String otherPerson;
    private Boolean uChargeOtherGuy;
    private Boolean uAreFrom;
    private double value;

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

        GetTransaction getTransaction = new GetTransaction(id);
        getTransaction.execute((Void)null);

        if(uChargeOtherGuy) {
            confirmReceive.setVisibility(View.VISIBLE);
            confirmFrom.setVisibility(View.VISIBLE);
        } else {
            confirmPay.setVisibility(View.VISIBLE);
            confirmTo.setVisibility(View.VISIBLE);
        }

        confirmValue.setText(String.valueOf(value));
        confirmPerson.setText(otherPerson);

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if(uAreFrom) {
            userSession.transactionConfirmFrom(id);
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    if(userSession.transactionConfirmedTo(id)) {
                        Toast.makeText(getApplicationContext(), "Transaction Complete!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }, 0, 200, TimeUnit.MILLISECONDS);

        } else {
            userSession.transactionConfirmTo(id);
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    if(userSession.transactionConfirmedFrom(id)) {
                        Toast.makeText(getApplicationContext(), "Transaction Complete!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }, 0, 200, TimeUnit.MILLISECONDS);

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
            if (success != null) {
                try {
                    JSONObject otherUser = (JSONObject)success.get("with");
                    otherPerson = otherUser.getString("name");
                    value = (double)success.get("value");

                    JSONObject fromUser = (JSONObject)success.get("from");
                    String fromName = fromUser.getString("name");
                    if(fromName.equalsIgnoreCase(otherPerson)) { //other initiated transaction
                        uAreFrom = false;
                        if(value < 0) { //other paying u
                            uChargeOtherGuy = true;
                        } else { //u paying other
                            uChargeOtherGuy = false;
                        }
                    } else { //u initiated transaction
                        uAreFrom = true;
                        if(value > 0) {
                            uChargeOtherGuy = true;
                        } else {
                            uChargeOtherGuy = false;
                        }
                    }

                } catch(JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Error processing transaction data", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
