package com.example.oo_raiser.rfidreaderapp.command;


public class Tools {

    //byte to string
    public static String Byte2HexString(byte[] b, int size)
    {
        String str = "";
        for(int i=0; i<size; i++)
        {
            String hex = Integer.toHexString(b[i]&0xFF );
            if(hex.length() == 1)
            {
                hex = "0" + hex;
            }
            str = str + hex.toUpperCase();
        }
        return str;
    }

    public static byte[] intToByte(int i)
    {
        byte[] abyte0 = new byte[3];
        abyte0[2] = (byte) (0xff & i);
        abyte0[1] = (byte) ((0xff00 & i) >> 8);
        abyte0[0] = (byte) ((0xff0000 & i) >> 16);
        return abyte0;
    }

}
