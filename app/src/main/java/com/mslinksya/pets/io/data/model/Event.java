package com.mslinksya.pets.io.data.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.mslinksya.pets.io.utils.Log;

import java.util.Date;

public class Event implements Parcelable {
    private final String ID;
    private final String device;
    private final String extra;
    private final Date timestamp;
    private String detectedPet;
    private Bitmap picture;

    public Event(String id, String device, String extra, String detectedPet, Date timestamp) {
        this.ID = id;
        this.device = device;
        this.extra = extra;
        this.detectedPet = detectedPet;
        this.timestamp = timestamp;
        this.picture = null;
        Log.d("Event", "New event with pet = " + detectedPet);
    }

    protected Event(Parcel in) {
        ID = in.readString();
        device = in.readString();
        extra = in.readString();
        detectedPet = in.readString();
        timestamp = new Date(in.readLong());
        picture = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public String getID() {
        return ID;
    }

    public String getDevice() {
        return device;
    }

    public String getExtra() {
        return extra;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getDetectedPet() {
        return detectedPet;
    }

    public void setDetectedPet(String detectedPet) {
        this.detectedPet = detectedPet;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ID);
        dest.writeString(this.device);
        dest.writeString(this.extra);
        dest.writeString(this.detectedPet);
        dest.writeLong(timestamp.getTime());
        dest.writeParcelable(picture, 0);
    }
}
