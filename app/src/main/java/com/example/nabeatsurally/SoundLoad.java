package com.example.nabeatsurally;


import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SoundLoad extends AppCompatActivity {

    private SoundPool soundPool_n;
    private int [] soundId_n = new int [41];
    private Context context;


    public SoundLoad(Context context, ArrayList <Integer> id_n) {
        this.context = context;

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                // USAGE_MEDIA
                // USAGE_GAME
                .setUsage(AudioAttributes.USAGE_GAME)
                // CONTENT_TYPE_MUSIC
                // CONTENT_TYPE_SPEECH, etc.
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();


        //soundPool_n = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPool_n = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        //ナベアツ音声
        for(int num_n = 0; num_n < id_n.size(); num_n++){
            soundId_n[num_n] = soundPool_n.load(this.context, id_n.get(num_n) , 1);
        }

    }

    public void play_n(int num){
        soundPool_n.play(soundId_n[num], 1.0f, 1.0f, 0, 0, 1.0f);
    }
}

