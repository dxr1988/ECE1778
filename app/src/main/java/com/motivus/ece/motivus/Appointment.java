package com.motivus.ece.motivus;

import android.os.Parcel;
import android.os.Parcelable;

class AppointmentStatistic{
    public float rate;
    public int accomplishedAppointment;
    public int totalAppointment;
}
/**
 * Created by dongx on 2015-02-18.
 */
public class Appointment implements Parcelable {
    public int id;
    public String title;
    public String detail;
    public String date;
    public String time;
    public double latitude;
    public double longitude;
    public int done = 0;
    public byte[] pic;

    public Appointment() {

    }

    public Appointment(Parcel in) {
        super();
        readFromParcel(in);
    }

    public Appointment(int id, String title, String detail, String date, String time, Double latitude, Double longitude, int done, byte[] pic) {
        super();
        this.id = id;
        this.title = title;
        this.detail = detail;
        this.date = date;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.done = done;
        this.pic = pic;
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.detail = in.readString();
        this.date = in.readString();
        this.time = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.done = in.readInt();
        in.readByteArray(this.pic);
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.detail);
        dest.writeString(this.date);
        dest.writeString(this.time);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeInt(this.done);
        dest.writeByteArray(this.pic);
    }

    public static final Creator<Appointment> CREATOR = new Creator<Appointment>() {
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };
}
