package com.mslinksya.pets.io.ui.createaccount;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class CreateAccountFormState {
    @Nullable
    private Integer usernameError;
    @Nullable
    private Integer passwordError;
    @Nullable
    private Integer useraliasError;
    private boolean isDataValid;

    CreateAccountFormState(@Nullable Integer usernameError, @Nullable Integer passwordError, @Nullable Integer useraliasError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.useraliasError = useraliasError;
        this.isDataValid = false;
    }

    CreateAccountFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.useraliasError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    @Nullable
    Integer getUserAliasError() {
        return useraliasError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}