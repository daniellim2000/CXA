package com.example.danie.schoolcashless;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.danie.schoolcashless.model.UserSession;
import com.example.danie.schoolcashless.model.exception.BadAuthenticationException;
import com.example.danie.schoolcashless.model.exception.BadResponseException;
import com.victor.loading.rotate.RotateLoading;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText mEmailView, mPasswordView, mConfirmPasswordView, mNameView;
    Button mRegisterView;
    RotateLoading mRotateLoadingView;
    View mRegisterFormView;

    UserRegisterTask mAuthTask = null;

    boolean showLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailView = (TextInputEditText) findViewById(R.id.register_username);
        mPasswordView = (TextInputEditText) findViewById(R.id.register_password);
        mConfirmPasswordView = (TextInputEditText) findViewById(R.id.register_confirmpassword);
        mNameView = (TextInputEditText) findViewById(R.id.register_name);
        mRegisterView = (Button) findViewById(R.id.register_btn_register);
        mRotateLoadingView = (RotateLoading) findViewById(R.id.rotateloading);
        mRegisterFormView = findViewById(R.id.register_form);

        mRegisterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

    private void register() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmpassword = mConfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        /*if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        // Check for a valid email address.
        if (name.length() == 0) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }
        if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_field_password_short));
            focusView = mPasswordView;
            cancel = true;
        }
        if (!password.equals(confirmpassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_field_password_mismatch));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserRegisterTask(name, email, password);
            mAuthTask.execute((Void) null);
        }
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Integer> {

        private final String mEmail;
        private final String mPassword;
        private final String mName;

        UserRegisterTask(String name, String email, String password) {
            mEmail = email;
            mPassword = password;
            mName = name;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                UserSession.createUser(mName, mEmail, mPassword);
            } catch (BadResponseException e) {
                e.printStackTrace();
                return R.string.error_misbehaving;
            } catch (IOException e) {
                e.printStackTrace();
                return R.string.error_connection;
            } catch (BadAuthenticationException e) {
                e.printStackTrace();
                return R.string.error_authenticate;
            }

            return 200;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mAuthTask = null;
            showProgress(false);

            if (success == 200) {
                finish();
                Intent intent = new Intent(RegisterActivity.this, SavingsActivity.class);
                RegisterActivity.this.startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), success, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void showProgress(boolean show) {
        showLoading = show;
        if (show) {
            mRotateLoadingView.setVisibility(View.VISIBLE);
            mRegisterFormView.setVisibility(View.GONE);
            mRotateLoadingView.start();
        } else {
            mRotateLoadingView.setVisibility(View.GONE);
            mRegisterFormView.setVisibility(View.VISIBLE);
            mRotateLoadingView.stop();
        }
    }
}
