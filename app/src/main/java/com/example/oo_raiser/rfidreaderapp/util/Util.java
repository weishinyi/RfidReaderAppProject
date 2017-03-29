package com.example.oo_raiser.rfidreaderapp.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.example.oo_raiser.rfidreaderapp.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by OO-Raiser on 2017/3/29.
 */

public class Util {
    public static SoundPool sp;
    public static Map<Integer, Integer> suondMap;
    public static Context context;

    //initialization Sound Pool
    public static void initSoundPool(Context context){
        Util.context = context;
        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        suondMap = new HashMap<Integer, Integer>();
        suondMap.put(1, sp.load(context, R.raw.msg, 1));
    }

    //play the sound of sound pool
    public static void play(int sound, int number){
        AudioManager am = (AudioManager)Util.context.getSystemService(Util.context.AUDIO_SERVICE);
        //返回當前AlarmManager最大音量
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //返回當前AudioManager對像的音量值
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = audioCurrentVolume/audioMaxVolume;

        sp.play(suondMap.get(sound),    //播放的音樂Id
                audioCurrentVolume,     //左聲道音量
                audioCurrentVolume,     //右聲道音量
                1,                      //優先等級，0為最低
                number,                 //循環次數，0不循環，-1永遠循環
                1);                     //回放速度，值在0.5-2.0之間，1為正常速度
    }

}
