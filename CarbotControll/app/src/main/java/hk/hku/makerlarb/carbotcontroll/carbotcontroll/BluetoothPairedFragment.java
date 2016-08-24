package hk.hku.makerlarb.carbotcontroll.carbotcontroll;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

import hk.hku.makerlarb.carbotcontroll.R;
import hk.hku.makerlarb.carbotcontroll.models.Bluetooth;

/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothPairedFragment extends Fragment {


    private List<Bluetooth> pairedDevices = new ArrayList<Bluetooth>();


    public BluetoothPairedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View viewHierachy = inflater.inflate(R.layout.fragment_bluetoothpaired_list, container, false);

//        pairedDevices = getArguments().getParcelableArrayList("Bluetooth paired");
        return viewHierachy;
    }

}
