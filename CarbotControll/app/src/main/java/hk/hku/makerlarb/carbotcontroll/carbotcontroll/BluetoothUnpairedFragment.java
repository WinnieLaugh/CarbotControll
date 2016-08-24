package hk.hku.makerlarb.carbotcontroll.carbotcontroll;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hk.hku.makerlarb.carbotcontroll.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothUnpairedFragment extends Fragment {


    public BluetoothUnpairedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewHierachy = inflater.inflate(R.layout.fragment_bluetoothunpaired_list, container, false);

        return viewHierachy;
    }

}
