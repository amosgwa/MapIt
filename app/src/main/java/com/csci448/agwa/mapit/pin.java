package com.csci448.agwa.mapit;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by amosgwa on 4/9/17.
 */

public class Pin {
    private Date time;
    private LatLng pos;

    public Pin(Date date, LatLng location) {
        this.time = date;
        this.pos = location;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public LatLng getPos() {
        return pos;
    }

    public void setPos(LatLng pos) {
        this.pos = pos;
    }
}
