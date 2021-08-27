package com.mslinksya.pets.io.data.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.mslinksya.pets.io.utils.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Event implements Parcelable {
    private static final String TAG = Event.class.getSimpleName();

    private final String ID;
    private final String device;
    private final String extra;
    private final Calendar timestamp;
    private String detectedPet;
    private Bitmap picture;

    public Event(String id, String device, String extra, String detectedPet, Date timestamp) {
        this.ID = id;
        this.device = device;
        this.extra = extra;
        this.detectedPet = detectedPet;
        this.picture = null;

        this.timestamp = Calendar.getInstance();
        this.timestamp.setTime(new Date(timestamp.getTime() + this.timestamp.getTimeZone().getRawOffset()));
//        Log.d("Event", "New event offset=" + this.timestamp.getTimeZone().getRawOffset());
//        Log.d("Event", "New event timezone=" + timestamp.toString());
//        Log.d("Event", "New event calendar=" + this.timestamp.toString());
//        this.timestamp.setTimeZone(TimeZone.getTimeZone("UTC"));
//        Log.d("Event", "New event prev calendar=" + this.timestamp.toString());
//        Log.d("Event", "New event with pet = " + detectedPet + ", timezone=" + this.timestamp.getTime());
//        Log.d("Event", "New event updated calendar=" + this.timestamp.toString());
    }

    protected Event(Parcel in) {
        ID = in.readString();
        device = in.readString();
        extra = in.readString();
        detectedPet = in.readString();
        timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(in.readLong());
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
        if (picture.getWidth() > 320 || picture.getHeight() > 240) {
            Log.w(TAG, "Recreating scaled bitmap (" + picture.getWidth() + "x" + picture.getHeight() + ")");
            this.picture = Bitmap.createScaledBitmap(picture, 320, 240, false);
        } else {
            Log.w(TAG, "Using original picture");
            this.picture = picture;
        }
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

    public Calendar getTimestamp() {
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
        dest.writeLong(timestamp.getTime().getTime());
        dest.writeParcelable(picture, 0);
    }
}
