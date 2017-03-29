package com.example.oo_raiser.rfidreaderapp.command;

import java.util.List;

/**
 * Created by OO-Raiser on 2017/3/24.
 */

public interface CommendManager {

    //設定鮑率
    public boolean setBaudrate();

    //取得韌體
    public byte[] getFirmware() ;

    //設定輸出Power
    public boolean setOutputPower(int value);

    //
    public List<InventoryInfo> inventoryRealTime();

    //選擇epc
    public void selectEPC(byte[] epc);

    //從板子讀取
    public byte[] readFrom6C(int memBank, int startAddr, int length, byte[] accessPassword);

    //從板子寫出
    public boolean writeTo6C(byte[] password, int memBank, int startAddr, int dataLen, byte[] data);

    //設定參數
    public void setSensitivity(int value);

    //鎖定標籤
    public boolean lock6C(byte[] password, int memBank, int lockType);

    //銷毀標籤
    public boolean killTag(byte[] password);

    public void close();

    public byte checkSum(byte[] data);

    public int setFrequency(int startFrequency, int freqSpace, int freqQuality);
}
