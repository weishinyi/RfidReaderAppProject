package com.example.oo_raiser.rfidreaderapp.command;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by OO-Raiser on 2017/3/24.
 */

public class NewSendCommendManager implements CommendManager{
    //region --- 物件宣告 ---
    private String TAG = "NewSendCommendManager";

    private InputStream in;
    private OutputStream out;

    private final byte HEAD = (byte) 0xAA;
    private final byte END = (byte) 0x8E;

    public static final int RESEVER_MENBANK = 0;
    public static final int EPC_MEMBANK = 1;
    public static final int TID_MEMBANK = 2;
    public static final int USER_MENBANK = 3;

    public static final byte RESPONSE_OK = 0x00;
    public static final byte ERROR_CODE_ACCESS_FAIL = 0x16;
    public static final byte ERROR_CODE_NO_CARD = 0x09;
    public static final byte ERROR_CODE_READ_SA_OR_LEN_ERROR = (byte) 0xA3;
    public static final byte ERROR_CODE_WRITE_SA_OR_LEN_ERROR = (byte) 0xB3;

    public static final int SENSITIVE_HIHG = 3;
    public static final int SENSITIVE_MIDDLE = 2;
    public static final int SENSITIVE_LOW = 1;
    public static final int SENSITIVE_VERY_LOW = 0;

    public static final int LOCK_TYPE_OPEN = 0;
    public static final int LOCK_TYPE_PERMA_OPEN = 1;
    public static final int LOCK_TYPE_LOCK = 2;
    public static final int LOCK_TYPE_PERMA_LOCK = 3;

    public static final int LOCK_MEM_KILL = 1;
    public static final int LOCK_MEM_ACCESS = 2;
    public static final int LOCK_MEM_EPC = 3; // EPC
    public static final int LOCK_MEM_TID = 4; // TID
    public static final int LOCK_MEM_USER = 5; // USER


    private byte[] selectEPC = null;
    //endregion

    //Constructor
    public NewSendCommendManager(InputStream is, OutputStream os)
    {
        in = is;
        out = os;
    }

    //region --- function ---
    private void sendCMD(byte[] cmd)
    {
        try {
            out.write(cmd);
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private byte[] read()
    {
        byte[] resp =null;

        int size = 0;
        byte[] buffer = new byte[256];
        byte[] temp = new byte[512];
        int index = 0;
        int count = 0;
        int cnt = 6;

        try{
            while (cnt>0){
                Thread.sleep(150);
                int ts = in.available();
                if(!(ts>0)){
                    cnt--;
                    continue;
                }

                size = in.read(buffer, 0, 256);
                if(size>0){
                    Log.i(TAG,"buffer: "+Tools.Byte2HexString(buffer,size));
                    count = count+size;

                    if(count>512)
                    {
                        count =0;
                        Arrays.fill(temp, (byte)0x00);
                    }

                    System.arraycopy(buffer, 0, temp, index, size);
                    index = index + size;
                    if(count>7)
                    {
                        if(temp[0] == HEAD)
                        {
                            int len =temp[4]&0xff;

                            if(count<len+7)
                                continue;
                            if(temp[len+6] != END)
                                continue;

                            resp = new byte[len+7];
                            System.arraycopy(temp, 0, resp, 0, len+7);
                            count = 0;
                            index = 0;
                            Arrays.fill(temp, (byte)0x00);
                            return resp;
                        }else {
                            count = 0;
                            index = 0;
                            Arrays.fill(temp, (byte)0x00);
                        }
                    }
                }
                cnt--;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return resp;
    }

    private byte[] handlerResponse(byte[] response)
    {
        byte[] data = null;
        byte crc = 0x00;
        int respLen = response.length;

        if(response[respLen-1]!=END)
        {
            return data;
        }
        if(respLen<7)
        {
            return data;
        }

        int lengthHigh = response[3]&0xff;
        int lengthLow = response[4]&0xff;
        int dataLen = lengthHigh*256 + lengthLow;

        //CRC
        crc = checkSum(response);
        if(crc!=response[respLen-2])
        {
            return data;
        }

        if(dataLen!=0 && respLen == dataLen+7)
        {
            data = new byte[dataLen+1];
            data[0] = response[2];
            System.arraycopy(response, 5, data, 1, dataLen);
        }

        return data;
    }

    public boolean setRecvParam(int mixer_g, int if_g, int trd) {
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0xF0, (byte) 0x00,(byte) 0x04, (byte) 0x03, (byte) 0x06, (byte) 0x01, (byte) 0xB0, (byte) 0xAE, END };
        byte[] recv = null;
        byte[] content = null;
        cmd[5] = (byte) mixer_g;
        cmd[6] = (byte) if_g;
        cmd[7] = (byte) (trd / 256);
        cmd[8] = (byte) (trd % 256);
        cmd[9] = checkSum(cmd);
        sendCMD(cmd);
        recv = read();
        if (recv != null) {
            content = handlerResponse(recv);
            if (content != null) {
                return true;
            }
        }
        return false;
    }

    public List<InventoryInfo> inventoryMulti()
    {
        List<InventoryInfo> list = new ArrayList<InventoryInfo>();
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0x27, (byte) 0x00,
                (byte) 0x03, (byte) 0x22, (byte) 0x27, (byte) 0x10,
                (byte) 0x83, END };
        sendCMD(cmd);

        byte[] response = this.read();
        if(response!=null)
        {
            int respLen = response.length;
            int start = 0;

            if(respLen>15)
            {
                while (respLen>5)
                {
                    InventoryInfo info = new InventoryInfo();
                    int paraLen = response[start + 4] & 0xff;
                    int singleCardLen = paraLen + 7;

                    if(singleCardLen > respLen){
                        break;
                    }

                    byte[] sigleCard = new byte[singleCardLen];
                    System.arraycopy(response, start, sigleCard, 0, singleCardLen);

                    byte[] resolve = handlerResponse(sigleCard);
                    if(resolve != null && paraLen > 5){
                        info.setRssi((resolve[1] & 0xff));
                        info.setPc(new byte[] { resolve[2], resolve[3] });
                        byte[] epcBytes = new byte[paraLen - 5];
                        System.arraycopy(resolve, 4, epcBytes, 0, paraLen-5);
                        info.setEpc(epcBytes);
                        list.add(info);
                    }
                    start = start + singleCardLen;
                    respLen = respLen - singleCardLen;
                }

            }else{
                handlerResponse(response);
            }
        }
        return  list;
    }

    public void stopInventoryMulti() {
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0x28, (byte) 0x00,  (byte) 0x00, (byte) 0x28, END };
        sendCMD(cmd);
        byte[] recv = read();
    }

    private void setSelectPara() {
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0x0C, (byte) 0x00,
                (byte) 0x13, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x20, (byte) 0x60, (byte) 0x00,
                (byte) 0x01, (byte) 0x61, (byte) 0x05, (byte) 0xB8,
                (byte) 0x03, (byte) 0x48, (byte) 0x0C, (byte) 0xD0,
                (byte) 0x00, (byte) 0x03, (byte) 0xD1, (byte) 0x9E,
                (byte) 0x58, END };
        if (this.selectEPC != null) {
            System.arraycopy(selectEPC, 0, cmd, 12, selectEPC.length);
            cmd[cmd.length - 2] = checkSum(cmd);
            Log.e("setSelectPara", Tools.Byte2HexString(cmd, cmd.length));
            sendCMD(cmd);
        }
    }

    private byte[] readMemBank(int memBank, int startAddr, int length, byte[] accessPassword, int count){
        this.setSelectPara();
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0x39, (byte) 0x00,
                (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x08, (byte) 0x4D, END };
        byte[] data = null;

        if (accessPassword == null || accessPassword.length != 4) {
            return null;
        }

        System.arraycopy(accessPassword, 0, cmd, 5, 4);
        cmd[9] = (byte) memBank;
        if (startAddr <= 255) {
            cmd[10] = 0x00;
            cmd[11] = (byte) startAddr;
        } else {
            int addrH = startAddr / 256;
            int addrL = startAddr % 256;
            cmd[10] = (byte) addrH;
            cmd[11] = (byte) addrL;
        }

        if (length <= 255) {
            cmd[12] = 0x00;
            cmd[13] = (byte) length;
        } else {
            int lengH = length / 256;
            int lengL = length % 256;
            cmd[12] = (byte) lengH;
            cmd[13] = (byte) lengL;
        }
        cmd[14] = checkSum(cmd);
        sendCMD(cmd);

        byte[] response = this.read();
        if (response != null)
        {
            byte[] resolve = handlerResponse(response);
            if (resolve != null)
            {
                if(resolve[0] == (byte) 0x39)
                {
                    int lengData = resolve.length - resolve[1] - 2;
                    data = new byte[lengData];
                    System.arraycopy(resolve, resolve[1] + 2, data, 0, lengData);
                }else{
                    data = new byte[1];
                    data[0] = resolve[1];
                    count--;
                    if(count > 0){
                        data = readMemBank(memBank, startAddr, length, accessPassword, count);
                    }
                }
            }
        }
        return data;
    }

    private boolean writeMemback(byte[] password, int memBank, int startAddr, int dataLen, byte[] data, int count){
        this.setSelectPara();
        if (password == null || password.length != 4) {
            return false;
        }

        boolean writeFlag = false;
        int cmdLen = 16 + data.length;
        int parameterLen = 9 + data.length;
        byte[] cmd = new byte[cmdLen];
        cmd[0] = HEAD;
        cmd[1] = 0x00;
        cmd[2] = 0x49;

        if (parameterLen < 256) {
            cmd[3] = 0x00;
            cmd[4] = (byte) parameterLen;
        } else {
            int paraH = parameterLen / 256;
            int paraL = parameterLen % 256;
            cmd[3] = (byte) paraH;
            cmd[4] = (byte) paraL;
        }

        System.arraycopy(password, 0, cmd, 5, 4);
        cmd[9] = (byte) memBank;

        if (startAddr < 256) {
            cmd[10] = 0x00;
            cmd[11] = (byte) startAddr;
        } else {
            int startH = startAddr / 256;
            int startL = startAddr % 256;
            cmd[10] = (byte) startH;
            cmd[11] = (byte) startL;
        }

        if (dataLen < 256) {
            cmd[12] = 0x00;
            cmd[13] = (byte) dataLen;
        } else {
            int dataLenH = dataLen / 256;
            int dataLenL = dataLen % 256;
            cmd[12] = (byte) dataLenH;
            cmd[13] = (byte) dataLenL;
        }

        System.arraycopy(data, 0, cmd, 14, data.length);
        cmd[cmdLen - 2] = checkSum(cmd);
        cmd[cmdLen - 1] = END;
        sendCMD(cmd);
        try{
            Thread.sleep(50);
        }catch (Exception e){
            e.printStackTrace();
        }

        byte[] response = this.read();
        if (response != null){
            byte[] resolve = this.handlerResponse(response);
            if(resolve != null){
                if(resolve[0] == 0x49 && resolve[resolve.length - 1] == RESPONSE_OK){
                    writeFlag = true;
                } else {
                    count--;
                    if(count > 0){
                        writeFlag = writeMemback(password, memBank, startAddr, dataLen, data, count);
                    }
                }
            }
        }

        return writeFlag;
    }

    public int setWorkArea(int area){
        // BB 00 07 00 01 01 09 7E
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0x07, (byte) 0x00,
                (byte) 0x01, (byte) 0x01, (byte) 0x09, END };
        cmd[5] = (byte) area;
        cmd[6] = checkSum(cmd);
        sendCMD(cmd);
        byte[] recv = read();
        if (recv != null) {
            handlerResponse(recv);
        }
        return 0;
    }

    public int getOuputPower() {
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0xB7, (byte) 0x00,
                (byte) 0x00, (byte) 0xB7, END };
        byte[] recv;
        sendCMD(cmd);
        recv = this.read();
        if (recv != null) {
            byte[] resp = handlerResponse(recv);
            if (resp != null && resp.length > 2) {
                int value = ((resp[1] & 0xff) * 256 + (resp[2] & 0xff)) / 100;
                return value;
            }
        }
        return -1;
    }

    public int getFrequency() {
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0xAA, (byte) 0x00,
                (byte) 0x00, (byte) 0xAA, END };
        sendCMD(cmd);
        byte[] recv = read();
        if (recv != null) {
            handlerResponse(recv);
        }
        return 0;
    }

    //endregion

    //region --- override functions ---
    @Override
    public boolean setBaudrate() {
        byte[] cmd = {};
        return false;
    }

    @Override
    public byte[] getFirmware() {
        byte[] cmd = {HEAD, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x04, END};
        byte[] version = null;
        sendCMD(cmd);
        byte[] response = this.read();
        if(response!=null)
        {
            byte[] resolve = handlerResponse(response);
            if(response!=null && resolve.length>1){
                version = new byte[resolve.length-1];
                System.arraycopy(resolve, 1, version, 0, resolve.length-1);
            }
        }
        return version;
    }

    @Override
    public boolean setOutputPower(int value) {
        boolean bool=false;

        byte[] cmd = {HEAD ,(byte)0x00 ,(byte)0xB6 ,(byte)0x00,(byte)0x02 ,(byte)0x0A ,(byte)0x28,(byte)0xEA ,END};
        cmd[5] = (byte)((0xff00 & value)>>8);
        cmd[6] = (byte)(0xff & value);
        cmd[cmd.length - 2] = checkSum(cmd);
        sendCMD(cmd);
        byte[] recv = read();

        if(recv != null){
            byte[] resp = handlerResponse(recv);
            if(resp != null)
                bool = true;
        }
        return bool;
    }

    @Override
    public List<InventoryInfo> inventoryRealTime() {
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x00, (byte) 0x22, END };
        sendCMD(cmd);
        List<InventoryInfo> list = new ArrayList<InventoryInfo>();
        byte[] response = this.read();
        if(response!=null)
        {
            int respLen = response.length;
            int start = 0;
            if(respLen>15)
            {
                while (respLen>5)
                {
                    int paraLen = response[start + 4] & 0xff;
                    int singleCardLen = paraLen + 7;

                    if (singleCardLen > respLen)
                        break;
                    byte[] sigleCard = new byte[singleCardLen];
                    System.arraycopy(response, start, sigleCard, 0, singleCardLen);

                    byte[] resolve = handlerResponse(sigleCard);
                    InventoryInfo info = new InventoryInfo();

                    if(resolve != null && paraLen > 5)
                    {
                        info.setRssi((resolve[1] & 0xff));
                        info.setPc(new byte[] { resolve[2], resolve[3] });
                        byte[] epcBytes = new byte[paraLen - 5];
                        System.arraycopy(resolve, 4, epcBytes, 0, paraLen-5);
                        info.setEpc(epcBytes);
                        list.add(info);
                    }
                    start += singleCardLen;
                    respLen -= singleCardLen;
                }
            }else {
                handlerResponse(response);
            }
        }
        return list;
    }

    @Override
    public void selectEPC(byte[] epc) {
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0x12, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x13, END };
        this.selectEPC = epc;
    }

    @Override
    public byte[] readFrom6C(int memBank, int startAddr, int length, byte[] accessPassword) {
        int count = 2 ;
        return readMemBank(memBank, startAddr, length, accessPassword, count);
    }

    @Override
    public boolean writeTo6C(byte[] password, int memBank, int startAddr, int dataLen, byte[] data) {
        int count = 2 ;
        return writeMemback(password, memBank, startAddr, dataLen, data, count);
    }

    @Override
    public void setSensitivity(int value) {
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0xF0, (byte) 0x00,(byte) 0x04, (byte) 0x02, (byte) 0x06, (byte) 0x00,(byte) 0xA0, (byte) 0x9C, END };
        cmd[5] = (byte) value;
        cmd[cmd.length - 2] = checkSum(cmd);
        sendCMD(cmd);
    }

    @Override
    public boolean lock6C(byte[] password, int memBank, int lockType) {
        this.setSelectPara();
        byte[] cmd = { HEAD, 0x00, (byte) 0x82, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, END };

        byte[] recv;
        int lockPay = 0;
        byte[] lockPara = new byte[3];

        if(lockType == LOCK_TYPE_OPEN){
            lockPay = (1 << (20 - 2 * memBank + 1));
        }

        if(lockType == LOCK_TYPE_PERMA_OPEN){
            lockPay = (1 << (20 - 2 * memBank + 1)) + (1 << (20 - 2 * memBank)) + (1 << (2 * (5 - memBank)));
        }

        if (lockType == LOCK_TYPE_LOCK) {
            lockPay = (1 << (20 - 2 * memBank + 1)) + (2 << (2 * (5 - memBank)));
        }

        if (lockType == LOCK_TYPE_PERMA_LOCK) {
            lockPay = (1 << (20 - 2 * memBank + 1)) + (1 << (20 - 2 * memBank)) + (3 << (2 * (5 - memBank)));
        }

        lockPara = Tools.intToByte(lockPay);
        System.arraycopy(password, 0, cmd, 5, password.length);
        System.arraycopy(lockPara, 0, cmd, 9, lockPara.length);
        cmd[cmd.length - 2] = checkSum(cmd);
        sendCMD(cmd);

        recv = this.read();
        if (recv != null) {
            byte[] resp = handlerResponse(recv);
            if (resp != null){
                if (resp[0] == (byte) 0x82) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean killTag(byte[] password) {
        setSelectPara();
        boolean flag = false ;
        // bb 00 65 00 04 00 00 ff ff 67 7e
        byte[] cmd = { HEAD, 0x00, (byte) 0x65, (byte) 0x00, (byte) 0x04,
                (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x67, END };
        byte[] recv ;
        System.arraycopy(cmd, 4, password, 0, password.length);
        cmd[cmd.length - 2] = checkSum(cmd);
        sendCMD(cmd);
        recv = this.read();
        if(recv != null){
            byte[] resp = handlerResponse(recv);
            if(resp != null){
                if(resp[0] == (byte)0x65){
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public void close() {
        try {
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte checkSum(byte[] data) {
        byte crc = 0x00;
        for (int i = 1; i < data.length - 2; i++) {
            crc += data[i];
        }
        return crc;
    }

    @Override
    public int setFrequency(int startFrequency, int freqSpace, int freqQuality) {
        int frequency = 1;
        if (startFrequency > 840125 && startFrequency < 844875) {// 笢弊1
            frequency = (startFrequency - 840125) / 250;
        } else if (startFrequency > 920125 && startFrequency < 924875) {// 笢弊2
            frequency = (startFrequency - 920125) / 250;
        } else if (startFrequency > 865100 && startFrequency < 867900) {// 韁粔
            frequency = (startFrequency - 865100) / 200;
        } else if (startFrequency > 902250 && startFrequency < 927750) {// 藝弊
            frequency = (startFrequency - 902250) / 500;
        }
        byte[] cmd = { HEAD, (byte) 0x00, (byte) 0xAB, (byte) 0x00,
                (byte) 0x01, (byte) 0x04, (byte) 0xB0, END };
        cmd[5] = (byte) frequency;
        cmd[6] = checkSum(cmd);
        sendCMD(cmd);
        return 0;
    }
    //endregion



}
