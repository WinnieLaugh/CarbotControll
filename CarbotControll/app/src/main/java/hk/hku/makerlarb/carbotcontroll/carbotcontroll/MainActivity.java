package hk.hku.makerlarb.carbotcontroll.carbotcontroll;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import android.content.Context;
import android.content.Intent;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;

import hk.hku.makerlarb.carbotcontroll.Manifest;
import hk.hku.makerlarb.carbotcontroll.R;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager = null;
    private Sensor sensor;

    private static final int stand = 0, left = 1, right = 2, forward = 3, back = 4;
    private static final int swing = 0, swipe = 1;
    private int state = stand;
    private int stateBefore = state;
    private int xBefore=0, yBefore = 0, moveX = 0, moveY = 0;
    private boolean pressedState = false;
    private final static int permissionState = 0;
    private boolean permitted = false;

    private static Handler handler = new Handler();
    private static Thread thread = new Thread();

    private ImageButton controlButton;
    private Button bluetoothButton;
    private Button modeButton;
    private View thisView;
    private int count = 0;

    private int mode = swing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the listener for the control button in the center,
        controlButton = (ImageButton)findViewById(R.id.controlButton);
        controlButton.setOnTouchListener(controlListener);

        //set the listener for the view for the swipe mode
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        thisView = findViewById(R.id.myView);

        //get the sensor manager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //set the listener for the bluetooth connect button
        bluetoothButton = (Button)findViewById(R.id.bluetooth_connect);
        bluetoothButton.setOnClickListener(bluetoothConnect);

        //set listener for the mode-choose button
        modeButton = (Button)findViewById(R.id.mode_button);
        modeButton.setOnClickListener(modeChange);

        switchChanged();

    }

    @Override
    protected void onDestroy() {

        //unregister the sensor listener before destroy the activity
        sensorManager.unregisterListener(this);

        if (BluetoothActivity.socket.isConnected()) {
            try {
                //close the socket
                BluetoothActivity.socket.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    protected  void onRestart(){
        super.onRestart();
    }

    //set the whether or not to use the mobile phone to control
    public void switchChanged() {

        final Switch controlSwitch = (Switch) findViewById(R.id.switch_button);
        controlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {
                final boolean checked = ischecked;

                    if (BluetoothActivity.socket != null) {
                        if (BluetoothActivity.socket.isConnected()) {
                            Thread cmdthread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    handler.postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                                try {
                                                    byte[] cmd = {'g'};
                                                    if(checked){
                                                        cmd[0] = 'f';
                                                    }
                                                    BluetoothActivity.os.write(cmd);
                                                    BluetoothActivity.os.flush();
                                                    sleep(150);
                                                } catch (Exception e) {
                                                    Log.i("Error: ", e.toString());
                                                }
                                            }

                                    }, 150);
                                }
                            });
                            cmdthread.run();
                        }
                    }
                }


        });

    }

    //register the sensor after all things resumed
    @Override
    protected void onPostResume() {

        super.onPostResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    //while in sway mode, test the state and send commands
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        //the sway mode is only functional if the control button in the center is pressed
        //value[0] > 0 if the right side of the phone is lifted higher than the left side
        //and it is quite sensitive so I made the boundary 2
        //value[0] < 0 if the left side of the phone is lifted higher than the right side
        //value[1] > 0 if the up side of the phone is lifted (the screen of the phone is facing yourself)
        //value[1] < 0 if the down side of the phone is lifted(the screen of the phone is facing outside)
        if(pressedState){
            stateBefore = state;
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (sensorEvent.values[0] > 3) {
                    state = left;
                } else if (sensorEvent.values[0] < -3) {
                    state = right;
                } else if (sensorEvent.values[1] > 1) {
                    state = back;
                } else if(sensorEvent.values[1] < -1){
                    state = forward;
                }
            }

            //send the commands according to the state
            if (BluetoothActivity.socket != null) {
                if (BluetoothActivity.socket.isConnected()) {
                    thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                            handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                    try {
                                        byte[] cmd = {'f'};
                                        switch (state) {
                                            case left:
                                                cmd[0] = 'b';

                                                //send the commands by the output stream of the socket
                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();

                                                //if the commands are sent too fast, the car can not get that, it have to rest for some time here
                                                sleep(150);

                                                Log.i("Direction : ", "Left");

                                                break;

                                            case right:
                                                cmd[0] = 'd';

                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();

                                                sleep(150);

                                                Log.i("Direction : ", "Right");

                                                break;

                                            case back:
                                                cmd[0] = 'e';

                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();

                                                sleep(150);

                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();

                                                Log.i("Direction : ", "Back");
                                                break;

                                            case forward:
                                                cmd[0] = 'a';

                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();

                                                sleep(150);

                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();
                                                Log.i("Direction : ", "Forward");

                                                break;

                                        }

                                    } catch (Exception e) {
                                        Log.i("Error: ", e.toString());
                                    }
                                }
                        }, 150);

                        }
                    });
                }
              }
            }


        else{
            if (BluetoothActivity.socket != null) {
                if (BluetoothActivity.socket.isConnected()) {

                        thread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        //you just have to send the stand command once
                                        if(stateBefore != stand){
                                        try {
                                            count = 0;
                                            byte[] cmd = {'c'};
                                            BluetoothActivity.os.write(cmd);
                                            BluetoothActivity.os.flush();

                                            sleep(150);

                                            Log.i("Direction : ", "Stand");

                                            stateBefore =  stand;
                                        } catch (Exception e) {
                                            Log.i("Error: ", e.toString());
                                        }
                                    }
                                    }
                                }, 150);
                            }
                        });

                }
            }
        }

        //if the thread is running ,wait for it ends, it not, run the thread
        if(thread.isAlive()){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            thread.run();
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    //if the SDK is newer than SDK 23, this method has to be called to request the permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] Permissions, int[] grantResults){
        switch(requestCode){
            case permissionState:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    permitted = true;
                }else{
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, Permissions, grantResults);
        }
    }

    private void sleep(long ms){
        try{
            Thread.sleep(ms, 0);
        }catch (Exception e){
            Log.i("Thread Exception", e.toString());
        }
    }


    //the control listener for the control button in the center
    private View.OnTouchListener controlListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressedState = true;
                    break;
                case MotionEvent.ACTION_UP:
                    pressedState = false;
                    break;
            }

            return true;
        }
    };

    //the swipe button for the view of the swipe mode
    private View.OnTouchListener swipeListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            stateBefore = state;

            switch (motionEvent.getAction()){
                //if you pressed down on the view, get the x, y and use it to calculate the gesture
                case MotionEvent.ACTION_DOWN:
                    xBefore = (int)motionEvent.getX();
                    yBefore = (int)motionEvent.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveX = (int)motionEvent.getX();
                    moveY = (int)motionEvent.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    if(moveX - xBefore > 0
                            && (Math.abs(moveX - xBefore)) > 100){
                        Log.i("distance: ","" + (Math.abs(moveX - xBefore)));
                        state = right;

                    }else if(moveX - xBefore < 0
                            && (Math.abs(moveX - xBefore)) > 100){
                        Log.i("distance: ","" + (Math.abs(moveX - xBefore)));
                        state = left;

                    }else if(moveY - yBefore > 0
                            && (Math.abs(moveY - yBefore)) > 100){
                        Log.i("distance: ","" + (Math.abs(moveX - xBefore)));
                        state = back;

                    }else if(moveY - yBefore < 0
                            && (Math.abs(moveY - yBefore)) > 100){
                        Log.i("distance: ","" + (Math.abs(moveX - xBefore)));
                        state = forward;

                    }
                    break;

            }

            //send the commands
            if (BluetoothActivity.socket != null) {
                if (BluetoothActivity.socket.isConnected()) {
                    thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            handler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        byte[] cmd = {'f'};
                                        switch (state) {
                                            case left:
                                                cmd[0] = 'b';

                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();

                                                sleep(150);

                                                cmd[0] = 'c';

                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();

                                                Log.i("Direction : ", "Swipe Left");

                                                break;

                                            case right:
                                                cmd[0] = 'd';

                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();

                                                sleep(150);


                                                BluetoothActivity.os.write(cmd);
                                                BluetoothActivity.os.flush();

                                                Log.i("Direction : ", "Swipe Right");

                                                break;

                                            case back:
                                                    cmd[0] = 'e';

                                                    BluetoothActivity.os.write(cmd);
                                                    BluetoothActivity.os.flush();

                                                    sleep(150);

                                                    BluetoothActivity.os.write(cmd);
                                                    BluetoothActivity.os.flush();

                                                    Log.i("Direction : ", "Swipe Back");
                                                break;

                                            case forward:
                                                    cmd[0] = 'a';

                                                    BluetoothActivity.os.write(cmd);
                                                    BluetoothActivity.os.flush();

                                                    sleep(150);

                                                    BluetoothActivity.os.write(cmd);
                                                    BluetoothActivity.os.flush();
                                                    Log.i("Direction : ", "Swipe Forward");

                                                break;

                                        }

                                    } catch (Exception e) {
                                        Log.i("Error: ", e.toString());
                                    }
                                }
                            }, 150);

                        }
                    });

                }
            }
        if(thread.isAlive()){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            thread.run();
        }

            return true;
        }
    };

    private View.OnClickListener bluetoothConnect = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            Log.i("Click information", "Clicked");
            if(Build.VERSION.SDK_INT >= 23){
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) !=
                        PackageManager.PERMISSION_GRANTED){

                    //if the SDk is higher than 23, and the permission is not granted
                    Log.i("Permission Test", "No Permission, SDK > 23");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, permissionState);
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN);
                }else{
                    Log.i("Permission Test", "Permitted, SDK > 23");
                    permitted = true;
                }
            }else{

                Log.i("Permission Test", "Permitted SDK < 23");
                permitted = true;
            }

            //start the bluetooth activity if  the permission is got
            if(permitted){
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        }
    };

    private View.OnClickListener modeChange = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
                mode = (mode + 1) % 2;

            switch (mode){
                case swing:
                    Toast.makeText(MainActivity.this, "Changed to swing mode", Toast.LENGTH_SHORT).show();
                    break;
                case swipe:
                    Toast.makeText(MainActivity.this, "Changed to swipe mode", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

            //register or unregister the listener
            switch (mode){
                case swing:
                    thisView.setOnTouchListener(null);
                    controlButton.setOnTouchListener(controlListener);
                    sensorManager.registerListener(MainActivity.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                    break;
                case swipe:
                    sensorManager.unregisterListener(MainActivity.this);
                    controlButton.setOnTouchListener(null);
                    pressedState = false;
                    thisView.setOnTouchListener(swipeListener);
                    controlButton.setOnClickListener(stopListener);
                    break;
                default:
                    break;
            }

        }
    };

    private View.OnClickListener stopListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            if (BluetoothActivity.socket != null) {
                if (BluetoothActivity.socket.isConnected()) {

                    thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            handler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    //you just have to send the stand command once
                                        try {
                                            count = 0;
                                            byte[] cmd = {'c'};
                                            BluetoothActivity.os.write(cmd);
                                            BluetoothActivity.os.flush();

                                            sleep(150);

                                            Log.i("Direction : ", "Stand");

                                            stateBefore =  stand;
                                        } catch (Exception e) {
                                            Log.i("Error: ", e.toString());
                                        }
                                    }
                            }, 150);
                        }
                    });

                }
            }
            if(thread.isAlive()){
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                thread.run();
            }

        }
    };

}
