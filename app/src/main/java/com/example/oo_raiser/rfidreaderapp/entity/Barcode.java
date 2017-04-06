package com.example.oo_raiser.rfidreaderapp.entity;

/**
 * Created by OO-Raiser on 2017/4/6.
 */

public class Barcode {
    private int BarcodeSeq;
    private String Barcode;
    private String BarcodeCreateTime;
    private int Car_ID;
    private String Car_Number;
    private int Emp_ID;
    private String Emp_Name;
    private int Loc_ID;
    private String Loc_Name;
    private String Loc_Address;
    private String UpdateTime;
    private int UpdateUserId;
    private int Count;


    //region --- get functions ---
    public int getBarcodeSeq() {
        return BarcodeSeq;
    }

    public String getBarcode() {
        return Barcode;
    }

    public String getBarcodeCreateTime() {
        return BarcodeCreateTime;
    }

    public int getCar_ID() {
        return Car_ID;
    }

    public String getCar_Number() {
        return Car_Number;
    }

    public int getEmp_ID() {
        return Emp_ID;
    }

    public String getEmp_Name() {
        return Emp_Name;
    }

    public int getLoc_ID() {
        return Loc_ID;
    }

    public String getLoc_Name() {
        return Loc_Name;
    }

    public String getLoc_Address() {
        return Loc_Address;
    }

    public String getUpdateTime() {
        return UpdateTime;
    }

    public int getUpdateUserId() {
        return UpdateUserId;
    }

    public int getCount() {
        return Count;
    }
    //endregion

    //region --- set functions ---

    public void setBarcodeSeq(int barcodeSeq) {
        BarcodeSeq = barcodeSeq;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public void setBarcodeCreateTime(String barcodeCreateTime) {
        BarcodeCreateTime = barcodeCreateTime;
    }

    public void setCar_ID(int car_ID) {
        Car_ID = car_ID;
    }

    public void setCar_Number(String car_Number) {
        Car_Number = car_Number;
    }

    public void setEmp_ID(int emp_ID) {
        Emp_ID = emp_ID;
    }

    public void setEmp_Name(String emp_Name) {
        Emp_Name = emp_Name;
    }

    public void setLoc_ID(int loc_ID) {
        Loc_ID = loc_ID;
    }

    public void setLoc_Name(String loc_Name) {
        Loc_Name = loc_Name;
    }

    public void setLoc_Address(String loc_Address) {
        Loc_Address = loc_Address;
    }

    public void setUpdateTime(String updateTime) {
        UpdateTime = updateTime;
    }

    public void setUpdateUserId(int updateUserId) {
        UpdateUserId = updateUserId;
    }

    public void setCount(int count) {
        Count = count;
    }


    //endregion

}
