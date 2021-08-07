package com.mslinksya.pets.io.ui.createaccount;

import android.content.Context;
import android.text.TextUtils;
import com.mslinksya.pets.io.utils.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.data.AccountRepository;
import com.mslinksya.pets.io.data.Result;
import com.mslinksya.pets.io.utils.Utils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateAccountViewModel extends ViewModel {

    private static final String TAG = CreateAccountViewModel.class.getSimpleName();

    private MutableLiveData<CreateAccountFormState> createAccountFormState = new MutableLiveData<>();
    private MutableLiveData<CreateAccountResult> createAccountResult = new MutableLiveData<>();
    private AccountRepository accountRepository;

    CreateAccountViewModel(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    LiveData<CreateAccountFormState> getCreateAccountFormState() {
        return createAccountFormState;
    }

    LiveData<CreateAccountResult> getCreateAccountResult() {
        return createAccountResult;
    }

    public void createAccount(Context context, String username, String password, String useralias, int numberOfPets) {
        // can be launched in a separate asynchronous job
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        new Thread(() ->
                future.complete(accountRepository.createAccount(context, username, password, useralias, numberOfPets))
        ).start();
        Result<Void> result;
        try {
            result = future.get(5000, TimeUnit.MILLISECONDS);
            Log.d(TAG, "got result for login");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            result = new Result.Error(new IOException("Error creating account"));
        }

        if (result instanceof Result.Success) {
            Log.d(TAG, "Account creation is success");
            createAccountResult.setValue(new CreateAccountResult());
        } else {
            Log.d(TAG, "Account creation is failure");
            createAccountResult.setValue(new CreateAccountResult(R.string.login_failed));
        }
    }

    public void createAccountDataChanged(String username, String password, String passwordConfirmation, String useralias, int numberOfPets) {
        if (!Utils.isUserNameValid(username)) {
            createAccountFormState.setValue(new CreateAccountFormState(R.string.invalid_username, null, null));
        } else if (!Utils.isPasswordValid(password)) {
            createAccountFormState.setValue(new CreateAccountFormState(null, R.string.invalid_password, null));
        } else if (!TextUtils.equals(password, passwordConfirmation)) {
            createAccountFormState.setValue(new CreateAccountFormState(null, R.string.mismatched_passwords, null));
        } else if (!Utils.isUserAliasValid(useralias)) {
            createAccountFormState.setValue(new CreateAccountFormState(null, null, R.string.invalid_useralias));
        } else {
            createAccountFormState.setValue(new CreateAccountFormState(true));
        }
    }
}