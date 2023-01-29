package com.example.nabeatsurally;


import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

import uk.me.berndporr.iirj.Butterworth;

public class PlayModeActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    public int soundFile_n = 41; //サウンドファイルの数　ループ変更用// 　

    SoundLoad NumCountInstance;

    private TextView textView;
    private TextView Hithantei;
    public  int count = 0;

    public double sensorx;
    public double sensory;
    public double sensorz;

    private SensorManager sensorManager;
    private Sensor accel;

    //時間関係
    public LocalDateTime starttime = LocalDateTime.now();
    public LocalDateTime startendtime = LocalDateTime.now();
    public boolean timeflag = true;

    public boolean swing_hantei = false;
    public boolean hit_hantei = false;

    public boolean bl_hit_updown = true;
    public int hit_count = 0;
    public int hit_keep_thirty = 0;
    public int hitout = 0;

    public boolean bl_swing_updown = true;
    public double min_acc = 100.0;
    public double max_acc = 0.0;
    public int swing_count = 0;

    public boolean bl_onhit = false;
    public boolean bl_onswing = false;
    public int swing_and_hit = 0;
    public int swing_only;
    public int hit_only;

    public boolean hit_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playmode);

        //画面遷移の処理
        Button returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(v -> finish());

        //ここより下はセンサー系の処理
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);
        Hithantei = findViewById(R.id.textView3);
        accel = sensorManager.getDefaultSensor(
                Sensor.TYPE_LINEAR_ACCELERATION);

        Button buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(this);

        Button buttonStop = findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(this);

        ArrayList <Integer> arr_n = new ArrayList<>();

        for(int num = 1; num <= soundFile_n; num++ ) {
            arr_n.add(getResources().getIdentifier("n" + num, "raw", getPackageName()));
        }

        NumCountInstance = new SoundLoad(getApplicationContext(),arr_n);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.button_start){
            sensorManager.registerListener(this, accel,
                    SensorManager.SENSOR_DELAY_GAME);
        }else if(i == R.id.button_stop){
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listenerの登録
        sensorManager.registerListener(this, accel,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Listenerを解除
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double HighPassNorm;
        double LowPassNorm;
        long difftime;
        LocalDateTime nowtime;
        //フィルタ設定
        int order = 10;

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            sensorx = event.values[0];
            sensory = event.values[1];
            sensorz = event.values[2];

            //フィルタ処理のライブラリ呼び出し
            Butterworth butterworth_hx = new Butterworth();
            Butterworth butterworth_hy = new Butterworth();
            Butterworth butterworth_hz = new Butterworth();

            butterworth_hx.highPass(order,50.0,15);
            butterworth_hy.highPass(order,50.0,15);
            butterworth_hz.highPass(order,50.0,15);

            Butterworth butterworth_lx = new Butterworth();
            Butterworth butterworth_ly = new Butterworth();
            Butterworth butterworth_lz = new Butterworth();

            butterworth_lx.lowPass(order,50.0,3);
            butterworth_ly.lowPass(order,50.0,3);
            butterworth_lz.lowPass(order,50.0,3);


            double hx = butterworth_hx.filter(event.values[0]);
            double hy = butterworth_hy.filter(event.values[1]);
            double hz = butterworth_hz.filter(event.values[2]);

            double lx = butterworth_lx.filter(event.values[0]);
            double ly = butterworth_ly.filter(event.values[1]);
            double lz = butterworth_lz.filter(event.values[2]);

            //加速度　-> ノルム　√ [絶対値](x^2 + y^2 + z^2)
            HighPassNorm = Math.sqrt(Math.abs(Math.pow(hx,2) + Math.pow(hy,2) + Math.pow(hz,2))) * 10000;
            LowPassNorm = Math.sqrt(Math.abs(Math.pow(lx,2) + Math.pow(ly,2) + Math.pow(lz,2))) * 10000000;

            //時間取得
            nowtime = LocalDateTime.now();
            if(timeflag){
                difftime = ChronoUnit.MILLIS.between(starttime,startendtime);
                timeflag = false;
            }else{
                difftime = ChronoUnit.MILLIS.between(starttime,nowtime);
            }

            hit(HighPassNorm,difftime);
            swing(LowPassNorm);



            if(hit_flag){
                sound_n(swing_and_hit);
                hit_flag = false;
            }



            Hithantei.setText(String.valueOf(swing_and_hit));


            String accelero;
            accelero = String.format(Locale.US,
                    "X: %.3f\nY: %.3f\nZ: %.3f",
                    event.values[0],event.values[1],event.values[2]);
            textView.setText(accelero);
        }
    }

    public void hit(double acc_num,Long Nowtime){
        //持ってくる値はハイパスかけた後のノルムデータと時間
        if (Nowtime > 4000){
            //開始4秒はカウントしない
            if(bl_hit_updown){
                ///5秒（hit_out ３００回データ）経過するとカウントを０にする
                hitout += 1;
                if(hitout >= 300){
                    hit_count = 0;
                    swing_and_hit = 0;
                }
                //trueの場合、ハイパス後ノルムが1.0くらいを越えると１回カウント。
                if(acc_num > 1.0){
                    hit_count += 1;
                    hit_hantei = true;
                    bl_hit_updown = false;
                    hitout = 0;
                    bl_onhit = true;          //スイングヒットに使用
                }
            }
            else if(!bl_hit_updown){
                //falseの場合、30回データが送り込まれる(0.6秒)までヒット回数を数えないように
                hit_keep_thirty += 1;
                if(hit_keep_thirty >= 30){
                    bl_hit_updown = true;
                    hit_keep_thirty = 0;
                }
            }
        }

    }

    public void swing(double acc_num){
        //持ってくる値はローパスかけた後のノルムデータ
        if (acc_num > max_acc){
            //maxより大きかった場合置き換え
            max_acc = acc_num;
        }
        else if (acc_num <max_acc){
            //maxから加速度が下がった最初のタイミングでスイング推定を行う。
            //極大値と極小値の差を求め、diffnumが10以上だった場合スイングと推定。極小値のリセットとして極大値を入れる
            if (bl_swing_updown){
                double diffnum = max_acc - min_acc;

                //カウント処理に向かう（スイングfalseケース)
                if (diffnum > 10.0){
                    swing_count += 1;
                    swing_hantei = true;
                    bl_onswing = true;      //スイングヒットのカウントに使用。
                }
                SwingHitCount();        //カウント処理に向かう（スイングtrueケース）
                min_acc = max_acc;
                bl_swing_updown = false;
            }
        }

        if(acc_num < min_acc){
            //minより小さかった場合置き換え　
            min_acc = acc_num;
        }
        else if (acc_num > min_acc){
            //minから加速度が上がった最初のタイミングで極大値をリセット。極大値に極小値を入れる。
            if (!bl_swing_updown){
                max_acc = min_acc;
                bl_swing_updown = true;
            }
        }
    }

    public void SwingHitCount(){
        //スイングのみ、ヒットのみ、両方、それ以外を設定
        if (bl_onhit && bl_onswing){
            swing_and_hit += 1;
            bl_onhit = false;
            bl_onswing = false;
            hit_flag = true;
        }
        else if (!bl_onhit && bl_onswing){
            swing_only += 1;
            bl_onswing = false;
            hit_flag =false;
        }
        else if(bl_onhit && !bl_onswing){
            hit_only += 1;
            bl_onhit = false;
            hit_flag = false;
        }
        else if (!bl_onhit && !bl_onswing){
            hit_flag = false;
        }
    }

    public void sound_n(int count){

        int Soundcount = count - 1;

        //音声呼び出し
        if(Soundcount <= 41) {
            NumCountInstance.play_n(Soundcount);
        }else{
            NumCountInstance.play_n(41);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}