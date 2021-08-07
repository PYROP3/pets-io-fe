package com.mslinksya.pets.io.ui.createaccount;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.utils.Constants;
import com.mslinksya.pets.io.utils.Utils;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = CreateAccountActivity.class.getSimpleName();

    private CreateAccountViewModel createAccountViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_create_account);
        createAccountViewModel = new ViewModelProvider(this, new CreateAccountViewModelFactory())
                .get(CreateAccountViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText passwordConfirmationEditText = findViewById(R.id.passwordConfirmation);
        final EditText userAliasEditText = findViewById(R.id.useralias);
        final EditText numberOfPetsEditText = findViewById(R.id.numberOfPets);
        final Button createAccountButton = findViewById(R.id.createAccount);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        if (intent != null) {
            usernameEditText.setText(intent.getStringExtra(Constants.USER_NAME_KEY));
            passwordEditText.setText(intent.getStringExtra(Constants.USER_PASS_KEY));
        }

        createAccountViewModel.getCreateAccountFormState().observe(this, new Observer<CreateAccountFormState>() {
            @Override
            public void onChanged(@Nullable CreateAccountFormState createAccountFormState) {
                if (createAccountFormState == null) {
                    return;
                }
                createAccountButton.setEnabled(createAccountFormState.isDataValid());
                if (createAccountFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(createAccountFormState.getUsernameError()));
                }
                if (createAccountFormState.getUserAliasError() != null) {
                    userAliasEditText.setError(getString(createAccountFormState.getUserAliasError()));
                }
                if (createAccountFormState.getPasswordError() != null) {
                    switch (createAccountFormState.getPasswordError()) {
                        case R.string.invalid_password:
                            passwordEditText.setError(getString(createAccountFormState.getPasswordError()));
                            break;
                        case R.string.mismatched_passwords:
                            passwordConfirmationEditText.setError(getString(createAccountFormState.getPasswordError()));
                            break;
                        default:
                            Log.w(TAG, "Unknown password error: " + createAccountFormState.getPasswordError());
                            break;
                    }
                }
            }
        });

        createAccountViewModel.getCreateAccountResult().observe(this, new Observer<CreateAccountResult>() {
            @Override
            public void onChanged(@Nullable CreateAccountResult createAccountResult) {
                Log.d(TAG, "onChanged");
                if (createAccountResult == null) {
                    Log.d(TAG, "onChanged : null");
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (createAccountResult.getError() != null) {
                    Log.d(TAG, "onChanged : error");
                }
                if (createAccountResult.isSuccess()) {
                    Log.d(TAG, "onChanged : success");
                    setResult(Activity.RESULT_OK);

                    //Complete and destroy createAccount activity once successful
                    finish();
                }
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
                createAccountViewModel.createAccountDataChanged(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        passwordConfirmationEditText.getText().toString(),
                        userAliasEditText.getText().toString(),
                        getNumberOfPets());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordConfirmationEditText.addTextChangedListener(afterTextChangedListener);
        userAliasEditText.addTextChangedListener(afterTextChangedListener);
        numberOfPetsEditText.addTextChangedListener(afterTextChangedListener);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                createAccountViewModel.createAccount(getApplicationContext(),
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        userAliasEditText.getText().toString(),
                        getNumberOfPets());
            }
        });
    }

    private void showCreateAccountFailed(@StringRes Integer errorString) {
        runOnUiThread(() -> Toast.makeText(
                getApplicationContext(), errorString, Toast.LENGTH_SHORT).show());
    }

    private int getNumberOfPets() {
        return Utils.parseIntOrDefault(((EditText) findViewById(R.id.numberOfPets)).getText().toString(), -1);
    }
}