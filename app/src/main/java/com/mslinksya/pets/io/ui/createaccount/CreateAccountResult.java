package com.mslinksya.pets.io.ui.createaccount;

import androidx.annotation.Nullable;

class CreateAccountResult {
    @Nullable
    private Boolean success;
    @Nullable
    private Integer error;

    CreateAccountResult(@Nullable Integer error) {
        this.error = error;
        this.success = false;
    }

    CreateAccountResult() {
        this.success = true;
    }

    boolean isSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }
}
