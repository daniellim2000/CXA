package com.example.danie.schoolcashless;

import android.content.Intent;
import android.content.res.Resources;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danie.schoolcashless.model.UserSession;
import com.example.danie.schoolcashless.model.exception.BadAuthenticationException;
import com.example.danie.schoolcashless.model.exception.BadResponseException;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText mUsername, mPassword;
    Button mRegister, mLogin;
    TextView mForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = (TextInputEditText) findViewById(R.id.login_username);
        mPassword = (TextInputEditText) findViewById(R.id.login_password);
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mLogin = (Button) findViewById(R.id.login_btn_login);
        mLogin.setClickable(false);
        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mUsername.getText().toString().length() > 0 && mPassword.getText().toString().length() > 0) {
                    mLogin.setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mUsername.getText().toString().length() > 0 && mPassword.getText().toString().length() > 0) {
                    mLogin.setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mRegister = (Button) findViewById(R.id.login_btn_register);
        mForgot = (TextView) findViewById(R.id.login_btn_forgot);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsername.getText().toString().length() > 0 && mPassword.getText().toString().length() > 0)
                    login();
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void login() {
        UserSession userSession = null;
        try {
            userSession = UserSession.createInstance(mUsername.getText().toString(), mPassword.getText().toString());
        } catch (BadResponseException e) {
            Toast.makeText(LoginActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (BadAuthenticationException e) {
            Toast.makeText(LoginActivity.this, "Username or password incorrect", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (userSession != null) {
            Intent intent = new Intent(this, SavingsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void register() {

    }
}
