package com.mslinksya.pets.io.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.Context;
import com.mslinksya.pets.io.utils.Log;
import android.util.Patterns;

import com.mslinksya.pets.io.data.LoginRepository;
import com.mslinksya.pets.io.data.Result;
import com.mslinksya.pets.io.data.model.LoggedInUser;
import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.utils.Utils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoginViewModel extends ViewModel {

    private static final String TAG = LoginViewModel.class.getSimpleName();

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(Context context, String username, String password) {
        // can be launched in a separate asynchronous job
        CompletableFuture<Result<LoggedInUser>> future = new CompletableFuture<>();
        new Thread(() ->
                future.complete(loginRepository.login(context, username, password))
        ).start();
        Result<LoggedInUser> result;
        try {
            result = future.get(5000, TimeUnit.MILLISECONDS);
            Log.d(TAG, "got result for login");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            result = new Result.Error(new IOException("Error logging in"));
        }

        if (result instanceof Result.Success) {
            Log.d(TAG, "login result is success");
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(LoggedInUserView.fromUser(data)));
        } else {
            Log.d(TAG, "login result is failure");
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!Utils.isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!Utils.isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }
}