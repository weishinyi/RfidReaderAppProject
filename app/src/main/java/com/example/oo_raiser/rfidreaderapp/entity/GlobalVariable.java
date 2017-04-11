package com.example.oo_raiser.rfidreaderapp.entity;

import android.app.Application;

/**
 * Created by OO-Raiser on 2017/4/11.
 */

public class GlobalVariable extends Application{

    private int userId;
    private int locId;

    //region --- Constructor ---
    public GlobalVariable() {
        this.userId = 1;
        this.locId = 1;
    }

    //endregion

    //region --- get ---
    public int getUserId() {
        return userId;
    }

    public int getLocId() {
        return locId;
    }
    //endregion


    //region --- set ---
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setLocId(int locId) {
        this.locId = locId;
    }
    //endregion

}
