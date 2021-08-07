package com.mslinksya.pets.io.data.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Pet implements Parcelable {
    private final String ID;
    private final String name;
    private Bitmap picture = null;

    protected Pet(String id, String name, Bitmap picture) {
        this.ID = id;
        this.name = name;
        this.picture = picture;
    }

    public Pet(String id, String name) {
        this.ID = id;
        this.name = name;
    }

    public static final Creator<Pet> CREATOR = new Creator<Pet>() {
        @Override
        public Pet createFromParcel(Parcel in) {
            String id = in.readString();
            String name = in.readString();
            Bitmap picture = in.readParcelable(Bitmap.class.getClassLoader());
            return new Pet(id, name, picture);
        }

        @Override
        public Pet[] newArray(int size) {
            return new Pet[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ID);
        dest.writeString(this.name);
        dest.writeParcelable(this.picture, 0);
    }

    public String getID() {
        return this.ID;
    }

    public String getName() {
        return this.name;
    }

    public Bitmap getPicture() {
        return this.picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }
}
