package com.example.danie.schoolcashless;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danie.schoolcashless.model.UserSession;
import com.example.danie.schoolcashless.model.exception.BadAuthenticationException;
import com.example.danie.schoolcashless.model.exception.BadResponseException;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SavingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SavingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavingsFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private List<Transaction> transactionList;
    private TransactionAdapter transactionAdapter;
    private TextView mBalanceView;

    private GetTransactionsTask mTransactionsTask;
    private GetBalanceTask mBalanceTask;
    private JSONArray jsonTransactions;
    private double mBalance;

    public SavingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SavingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SavingsFragment newInstance() {
        SavingsFragment fragment = new SavingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_savings, container, false);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.list_transactions);
        transactionList = new ArrayList<Transaction>();

        mBalanceView = (TextView) layout.findViewById(R.id.balance);

        transactionAdapter = new TransactionAdapter(transactionList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(transactionAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTransactionsTask = new GetTransactionsTask();
        mTransactionsTask.execute((Void) null);

        mBalanceView = (TextView) getView().findViewById(R.id.balance);
        mBalanceTask = new GetBalanceTask();
        mBalanceTask.execute((Void) null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSavingsFragmentInteraction(Uri uri);
    }

    private void getTransactions() throws JSONException, BadResponseException, IOException, BadAuthenticationException {
        transactionList.removeAll(transactionList);
        if (jsonTransactions != null) {
            for (int i = 0; i < jsonTransactions.length(); i++) {
                JSONObject json = jsonTransactions.getJSONObject(i);
                Number value = (Number) json.get("value");
                String id = (String) json.get("_id");
                Transaction t = new Transaction(id, value.doubleValue());
                transactionList.add(t);
            }
        }

        transactionAdapter.notifyDataSetChanged();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
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

    public class GetTransactionsTask extends AsyncTask<Void, Void, Integer> {

        GetTransactionsTask() {
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                jsonTransactions = UserSession.getInstance().getTransactions(0, 100);
            } catch (BadResponseException e) {
                e.printStackTrace();
                return R.string.error_misbehaving;
            } catch (IOException e) {
                e.printStackTrace();
                return R.string.error_connection;
            } catch (BadAuthenticationException e) {
                e.printStackTrace();
                return R.string.error_authenticate;
            } catch (JSONException e) {
                e.printStackTrace();
                return R.string.error_misbehaving;
            }

            return 200;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            if (success == 200) {
                try {
                    getTransactions();
                } catch (JSONException | BadResponseException | IOException | BadAuthenticationException e) {
                    e.printStackTrace();
                }
            } else {
                Snackbar.make(getActivity().findViewById(R.id.fab), success, Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTransactionsTask.execute();
                    }
                }).show();
            }
        }

    }

    public class GetBalanceTask extends AsyncTask<Void, Void, Integer> {

        private double mBalance;

        GetBalanceTask() {

        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                mBalance = UserSession.getInstance().getBalance();
            } catch (BadResponseException e) {
                e.printStackTrace();
                return R.string.error_misbehaving;
            } catch (IOException e) {
                e.printStackTrace();
                return R.string.error_connection;
            } catch (BadAuthenticationException e) {
                e.printStackTrace();
                return R.string.error_authenticate;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return 200;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            if (success == 200) {
                DecimalFormat df = new DecimalFormat("#.##");
                mBalanceView.setText("$" + df.format(mBalance));
            } else {
                Snackbar.make(getActivity().findViewById(R.id.fab), success, Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBalanceTask.execute();
                    }
                }).show();
            }
        }

    }

    public class GetTransactionDetailsTask extends AsyncTask<Void, Void, Integer> {

        private Transaction transaction;
        private JSONObject json;

        GetTransactionDetailsTask(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                json = UserSession.getInstance().getTransactionDetails(transaction.getId());
            } catch (BadResponseException e) {
                e.printStackTrace();
                return R.string.error_misbehaving;
            } catch (IOException e) {
                e.printStackTrace();
                return R.string.error_connection;
            } catch (BadAuthenticationException e) {
                e.printStackTrace();
                return R.string.error_authenticate;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return 200;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mTransactionsTask = null;

            if (success == 200) {
                try {
                    processTransactionDetails(transaction, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Snackbar.make(getActivity().findViewById(R.id.fab), success, Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBalanceTask.execute();
                    }
                }).show();
            }
        }

        @Override
        protected void onCancelled() {
            mTransactionsTask = null;
        }
    }

    private void processTransactionDetails(Transaction transaction, JSONObject json) throws JSONException {
        Number completed = (Number) json.get("completed");
        transaction.setUnixTime(completed.intValue());
        JSONObject with = (JSONObject) json.get("with");
        String name = with.getString("name");
        transaction.setWith(name);
    }

}
