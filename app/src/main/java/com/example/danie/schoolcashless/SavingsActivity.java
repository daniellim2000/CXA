package com.example.danie.schoolcashless;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danie.schoolcashless.model.UserSession;
import com.example.danie.schoolcashless.model.exception.BadAuthenticationException;
import com.example.danie.schoolcashless.model.exception.BadResponseException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SavingsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<Transaction> transactionList;
    private TransactionAdapter transactionAdapter;
    private TextView mBalanceView;

    private GetTransactionsTask mTransactionsTask;
    private GetBalanceTask mBalanceTask;
    private JSONArray jsonTransactions;
    private double mBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fabReceive = (FloatingActionButton) findViewById(R.id.fab_receive);
        fabReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(SavingsActivity.this, ScannerActivity.class), 0x0000c0de);
            }
        });

        FloatingActionButton fabSend = (FloatingActionButton) findViewById(R.id.fab_send);
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTransaction();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.list_transactions);
        transactionList = new ArrayList<Transaction>();

        transactionAdapter = new TransactionAdapter(transactionList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(transactionAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        mTransactionsTask = new GetTransactionsTask();
        mTransactionsTask.execute((Void) null);

        mBalanceView = (TextView) findViewById(R.id.balance);
        mBalanceTask = new GetBalanceTask();
        mBalanceTask.execute((Void) null);
    }

    private void getTransactions() throws JSONException, BadResponseException, IOException, BadAuthenticationException {
        if (jsonTransactions != null) {
            for (int i = 0; i < jsonTransactions.length(); i++) {
                JSONObject json = jsonTransactions.getJSONObject(i);
                String name = (String) json.get("from");
                Number value = (Number) json.get("value");
                Transaction t = new Transaction("", name, value.doubleValue());
                transactionList.add(t);
            }
        }

        transactionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private SavingsActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final SavingsActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    private void createTransaction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Transaction");
        View layout = getLayoutInflater().inflate(R.layout.dialog_payment, null, false);

        final TextInputEditText mAmountView = (TextInputEditText) layout.findViewById(R.id.dialog_payment_amount);
        final RadioButton mChargeButton = (RadioButton) layout.findViewById(R.id.dialog_charge);
        final RadioButton mCreditButton = (RadioButton) layout.findViewById(R.id.dialog_credit);

        mAmountView.addTextChangedListener(new TextWatcher() {
            DecimalFormat dec = new DecimalFormat("0.00");

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAmountView.removeTextChangedListener(this);

                if (mAmountView.getText().length() == 0 || mAmountView.getText().toString().charAt(0) != '$') {
                    mAmountView.setText("$" + mAmountView.getText().toString());
                    mAmountView.setSelection(mAmountView.length());
                }

                mAmountView.addTextChangedListener(this);
            }
        });

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (mAmountView.getText().toString().matches("[$]\\d*\\.\\d{2}"))
                    return "";
                return null;
            }
        };

        mAmountView.setFilters(new InputFilter[]{filter});

        builder.setView(layout);
        builder.setCancelable(true);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(SavingsActivity.this, QRCodeActivity.class);
                intent.putExtra("value", mAmountView.getText().toString().substring(1));

                if (mChargeButton.isChecked()) {
                    intent.putExtra("isCharge", true);
                } else if (mCreditButton.isChecked()) {
                    intent.putExtra("isCharge", false);
                }

                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    public class GetTransactionsTask extends AsyncTask<Void, Void, Integer> {

        GetTransactionsTask() {

        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                jsonTransactions = UserSession.getInstance().getTransactions(0, 100);
            } catch (BadResponseException e) {
                e.printStackTrace();
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            } catch (BadAuthenticationException e) {
                e.printStackTrace();
                return 2;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return 200;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mTransactionsTask = null;
            showProgress(false);

            if (success == 200) {
                try {
                    getTransactions();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (BadResponseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BadAuthenticationException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "" + success, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mTransactionsTask = null;
            showProgress(false);
        }
    }

    public class GetBalanceTask extends AsyncTask<Void, Void, Integer> {

        GetBalanceTask() {

        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                mBalance = UserSession.getInstance().getBalance();
            } catch (BadResponseException e) {
                e.printStackTrace();
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            } catch (BadAuthenticationException e) {
                e.printStackTrace();
                return 2;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return 200;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mTransactionsTask = null;
            showProgress(false);

            if (success == 200) {
                mBalanceView.setText("$" + mBalance);
            } else {
                Toast.makeText(getApplicationContext(), "" + success, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mTransactionsTask = null;
            showProgress(false);
        }
    }

    private void showProgress(boolean show) {

    }

}
