package hk.hku.makerlarb.carbotcontroll.carbotcontroll;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import hk.hku.makerlarb.carbotcontroll.R;
import hk.hku.makerlarb.carbotcontroll.adapters.BluetoothDeviceAdapter;
import hk.hku.makerlarb.carbotcontroll.listener.BltItemClickListener;
import hk.hku.makerlarb.carbotcontroll.models.Bluetooth;

public class BluetoothActivity extends FragmentActivity implements BltItemClickListener{

    private UUID SerialUUID;
    private static String DEFAULT_ADDRESS = "00:BA:55:57:17:D4";
    private static String addressToConnect = DEFAULT_ADDRESS;

    private ArrayList<Bluetooth> bondedList = new ArrayList<>();
    private ArrayList<Bluetooth> discoveredList = new ArrayList<>();
    private BluetoothDeviceAdapter boundedBltDeviceAdapter;
    private BluetoothDeviceAdapter discoveredBltDeviceAdapter;

    private RecyclerView pairedView;
    private RecyclerView discoveredView;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver receiver;

    public static  BluetoothSocket socket = null;
    public static OutputStream os;
    Thread thread;

    BluetoothDevice deviceDestinated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        pairedView = (RecyclerView)findViewById(R.id.bluetooth_paired_list);
        discoveredView = (RecyclerView)findViewById(R.id.bluetooth_unpaired_list);

        enableBluetooth();

        discoveredView.setLayoutManager(new LinearLayoutManager(this));
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                Log.i("Bluetooth action","BluetoothReceiver action = " + action);

                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    final BluetoothDevice scanDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(scanDevice == null || scanDevice.getName() == null)
                        return;

                    Log.i("Bluetooth info found", "name = " + scanDevice.getName() + "address " + scanDevice.getAddress());

                    final String scanDeviceName = scanDevice.getName(), scanDeviceAddress = scanDevice.getAddress();

                    BluetoothActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            for(int i=0; i <bondedList.size(); i++){
                                if(scanDeviceName.equals(bondedList.get(i).getName())){
                                    discoveredList.add(new Bluetooth(scanDeviceName, scanDeviceAddress));
                                    discoveredBltDeviceAdapter.notifyDataSetChanged();
                                }
                            }
                       }
                    });

                }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                    Log.i("Bluetooth discover", "finished");

                }

            }
        };

        discoveredBltDeviceAdapter = new BluetoothDeviceAdapter(this, discoveredList);
        discoveredView.setAdapter(discoveredBltDeviceAdapter);

        bluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onDestroy(){

        unregisterReceiver(receiver);

        super.onDestroy();



    }


    private void enableBluetooth(){
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }

        String name = bluetoothAdapter.getName();
        String address = bluetoothAdapter.getAddress();
        Log.i("Bluetooth info","this bluetooth name " + name + " address = "+ address);

        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        Log.i("Bluetooth info","this bluetooth bonded size = " + devices.size());

        for(BluetoothDevice bondedDevice: devices){
            bondedList.add(new Bluetooth(bondedDevice.getName(), bondedDevice.getAddress()));
            Log.i("Bluetooth bounded info:" ,"Bounded bluetooth name:" + bondedDevice.getName());
        }

        pairedView.setLayoutManager(new LinearLayoutManager(this));
        boundedBltDeviceAdapter = new BluetoothDeviceAdapter(this, bondedList);
        boundedBltDeviceAdapter.setOnItemClickListener(this);
        pairedView.setAdapter(boundedBltDeviceAdapter);
    }


    @Override
    public void OnItemClick(View view, final int position) {

        addressToConnect = bondedList.get(position).getAddress();


        thread = new Thread(new Runnable() {


            @Override
            public void run() {

                bluetoothAdapter.cancelDiscovery();

                deviceDestinated = bluetoothAdapter.getRemoteDevice(addressToConnect);

                SerialUUID = deviceDestinated.getUuids()[0].getUuid();


                while(socket == null ){
                    try{
                        socket = deviceDestinated.createRfcommSocketToServiceRecord(SerialUUID);
                    }catch (Exception e){
                        Log.w("Error", e.toString());
                    }
                }

                try{
                    if(socket.isConnected()){
                        Log.i("Connected socket","Yes, Connected");
                    }else{
                        Log.i("Connected socket","No,Not Connected");
                        try{
                            socket.connect();
                        }catch (Exception e){
                            Log.w("Error", e.toString());

                            try{
                                Log.e("", "trying fallback...");

                                socket = (BluetoothSocket)deviceDestinated.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(deviceDestinated,1);
                                socket.close();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                    os = socket.getOutputStream();

                }catch (Exception e){
                    Log.w("Error", e.toString());
                    try{
                        socket.close();
                        socket = null;
                    }catch (Exception closeException){
                        Toast.makeText(BluetoothActivity.this, "Unable to close socket during connection failure", Toast.LENGTH_SHORT).show();;
                    }
                }

            }
        });


        if(socket != null){
            try{
                thread.join();
            }catch (Exception e){
                Log.i("thread Exception", e.toString());
            }

        }else{
            thread.run();
        }


        if(socket != null){
            if(socket.isConnected()){

                Intent intent = new Intent(BluetoothActivity.this, MainActivity.class);
                startActivity(intent);

            }
        }

    }
}
