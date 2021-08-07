package com.mslinksya.pets.io.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import com.mslinksya.pets.io.utils.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.data.LoginRepository;
import com.mslinksya.pets.io.data.model.LoggedInUser;
import com.mslinksya.pets.io.fcm.MessagingService;
import com.mslinksya.pets.io.ui.createaccount.CreateAccountActivity;
import com.mslinksya.pets.io.ui.home.HomeActivity;
import com.mslinksya.pets.io.ui.login.LoginViewModel;
import com.mslinksya.pets.io.ui.login.LoginViewModelFactory;
import com.mslinksya.pets.io.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get fcm token for init
        MessagingService.getTokenAndDo(new MessagingService.FCMRunnable() {
            @Override
            public void run(String fcmToken) {
                Log.d(TAG, "FCM token is " + fcmToken);
            }
        });

        // Check if user is already logged in
        if (LoginRepository.getInstance().loadSharedPreferences(this) &&
        LoginRepository.getInstance().isLoggedIn()) {
            updateUiWithUser(LoggedInUserView.fromUser(LoginRepository.getInstance().getUser()));
            return;
        }

        setContentView(R.layout.activity_login);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final TextView createAccountButton = findViewById(R.id.createAccount);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                Log.d(TAG, "onChanged");
                if (loginResult == null) {
                    Log.d(TAG, "onChanged : null");
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    Log.d(TAG, "onChanged : error");
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    Log.d(TAG, "onChanged : success");
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                //finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(getApplicationContext(),
                            usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());

                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(getApplicationContext(),
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                intent.putExtra(Constants.USER_NAME_KEY, usernameEditText.getText().toString());
                intent.putExtra(Constants.USER_PASS_KEY, passwordEditText.getText().toString());
                LoginActivity.this.startActivity(intent);
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome, model.getDisplayName());
        runOnUiThread(() -> Toast.makeText(
                getApplicationContext(), welcome, Toast.LENGTH_SHORT).show());
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        runOnUiThread(() -> Toast.makeText(
                getApplicationContext(), errorString, Toast.LENGTH_LONG).show());
    }
}