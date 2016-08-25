package hk.hku.makerlarb.carbotcontroll.carbotcontroll;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.widget.Switch;


import java.io.IOException;

import hk.hku.makerlarb.carbotcontroll.R;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private SensorManager sensorManager = null;
    private Sensor sensor;

    private float[] angles = new float[3];

    private static final int left = 0, right = 1, stand = 2, back = 3;
    private int state = stand;

    ImageView shadowImg;
    Animation shadow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shadowImg = (ImageView) findViewById(R.id.shadow);
        shadow = AnimationUtils.loadAnimation(this, R.anim.upward);
        shadow.start();

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        switchChanged();

    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(this);

        if(BluetoothActivity.socket.isConnected()){
            try {
                BluetoothActivity.socket.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    public void switchChanged(){

        Switch controlSwitch = (Switch) findViewById(R.id.switch_button);
        controlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {

                if (ischecked) {
                    if(!adapter.isEnabled()){
                        adapter.enable();
                    }

                    Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    enable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
                    startActivity(enable);

                    Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                    startActivity(intent);
                } else {
                    disableBluetooth();
                }
            }
        });

    }

    private void disableBluetooth() {

    }

    @Override
    protected void onPostResume(){
            super.onPostResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int stateBefore = state;
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            if(sensorEvent.values[0] > 2){
                state = left;
            }else if(sensorEvent.values[0] < -2){
                state = right;
            }

            if(sensorEvent.values[1] > 2){
                state = back;
            }else {
                state = stand;
             }
        }


//        if(stateBefore != state){

                if(BluetoothActivity.os != null){
                    try{
                    byte[] cmd = {'c'};
                    switch (state){
                        case left:
                            cmd[0] = 'b';
                            BluetoothActivity.os.write(cmd);

                            shadow = AnimationUtils.loadAnimation(this, R.anim.left);
                            if(shadow.hasEnded())
                              shadow.start();
                            break;
                        case right:
                            cmd[0] = 'd';
                            BluetoothActivity.os.write(cmd);
                            shadow = AnimationUtils.loadAnimation(this, R.anim.right);
                            if(shadow.hasEnded())
                               shadow.start();
                            break;
                        case back:
                            cmd[0] = 'e';
                            BluetoothActivity.os.write(cmd);
                            shadow = AnimationUtils.loadAnimation(this, R.anim.downward);
                            if(shadow.hasEnded())
                                shadow.start();
                            break;
                        case stand:
                            cmd[0] = 'a';
                            BluetoothActivity.os.write(cmd);
                            shadow = AnimationUtils.loadAnimation(this, R.anim.upward);
                            if(shadow.hasEnded())
                                shadow.start();
                            break;
                    }

                    BluetoothActivity.os.flush();
                }catch (Exception e){
                    Log.i("Error: ", e.toString());
                }
                }

//        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
