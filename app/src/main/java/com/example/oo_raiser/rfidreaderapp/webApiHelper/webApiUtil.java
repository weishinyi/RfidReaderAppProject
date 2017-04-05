package com.example.oo_raiser.rfidreaderapp.webApiHelper;

/**
 * Created by OO-Raiser on 2017/4/2.
 */

public class webApiUtil {

    public final static String APPTAG = "RfidBoss";

    public final static String LOADING = "請稍後...";
    public final static String WAITING = "等待中...";

    public final static String ERROR = "錯誤";

    public final static String WIFI_CONNECTFAIL = "網路連線失敗...";
    public final static String SERVER_CONNENTFAIL = "伺服器連線失敗...";
    public final static String DOWNLOADING = "資料載入中...請稍後...";

    public final static String STATUS_UNSTART = "未執行";
    public final static String STATUS_START = "執行中";
    public final static String STATUS_DONE = "已完成";


    //Login
    public final static String SETTING = "setting";
    public final static String LOGIN_SUCCESS = "登入成功";
    public final static String LOGIN_FAIL = "登入失敗";

    //NewsDetail
    public final static String CONTENTWEB = "text/html; charset=UTF-8";

    //web api url
    public final static String QueryRecodeUrl = "http://140.134.26.173:1337/api/webapi/Get/";
    public final static String TodoyNewsUrl = "http://10.10.28.204:39643/api/TodayNews/";

    //token
    static public String statusCode(int Code) {
        String result = "";
        switch (Code) {
            case 200:
                result = "成功";
                break;
            case 400:
                result = "參數失敗";
                break;
            case 401:
                result = "使用者為認證";
                break;
            case 500:
                result = "未知的錯誤";
                break;
            case 503:
                result = "資料庫錯誤";
                break;
        }
        return result;
    }

}
