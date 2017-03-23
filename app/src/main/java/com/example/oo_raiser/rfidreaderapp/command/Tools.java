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



}
