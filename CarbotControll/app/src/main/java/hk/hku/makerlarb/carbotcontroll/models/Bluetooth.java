package hk.hku.makerlarb.carbotcontroll.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 8/21/2016.
 */
public class Bluetooth implements Parcelable{

    public String name;
    public String address;

    public Bluetooth(String name, String address){
        this.name = name;
        this.address = address;

    }

    public String getName(){
        return name;
    }

    public String getAddress(){
        return address;
    }

    protected Bluetooth(Parcel in) {
        name = in.readString();
        address = in.readString();
    }

    public static final Creator<Bluetooth> CREATOR = new Creator<Bluetooth>() {
        @Override
        public Bluetooth createFromParcel(Parcel in) {
            return new Bluetooth(in);
        }

        @Override
        public Bluetooth[] newArray(int size) {
            return new Bluetooth[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(address);
    }
}
