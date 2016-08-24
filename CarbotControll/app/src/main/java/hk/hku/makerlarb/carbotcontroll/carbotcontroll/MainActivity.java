package hk.hku.makerlarb.carbotcontroll.carbotcontroll;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import hk.hku.makerlarb.carbotcontroll.R;


public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView shadowImg = (ImageView) findViewById(R.id.shadow);
        Animation shadow = AnimationUtils.loadAnimation(this, R.anim.fly);
        shadowImg.startAnimation(shadow);

        switchChanged();
    }

    @Override
    protected void onDestroy() {
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


}
