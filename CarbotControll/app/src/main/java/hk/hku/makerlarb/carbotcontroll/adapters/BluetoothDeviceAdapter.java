package hk.hku.makerlarb.carbotcontroll.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import hk.hku.makerlarb.carbotcontroll.R;
import hk.hku.makerlarb.carbotcontroll.carbotcontroll.BluetoothPairedFragment;
import hk.hku.makerlarb.carbotcontroll.listener.BltItemClickListener;
import hk.hku.makerlarb.carbotcontroll.models.Bluetooth;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<Bluetooth> bluetoothDeviceList;
    private Context mContext;
    private BltItemClickListener listener;

    public BluetoothDeviceAdapter(Context context, List<Bluetooth> list){
        this.mContext = context;
        this.bluetoothDeviceList = list;
}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bluetooth_item, parent, false);

        return new ViewHolder(v, listener);
    }

    public void setOnItemClickListener(BltItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Bluetooth device = bluetoothDeviceList.get(position);

        holder.nameView.setText(device.getName());
        holder.addressView.setText(device.getAddress());

    }

    @Override
    public int getItemCount() {
        return bluetoothDeviceList == null? 0:bluetoothDeviceList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView nameView;
        public TextView addressView;
        private BltItemClickListener BltListener;

        public ViewHolder(View v, BltItemClickListener listener){
            super(v);

            nameView = (TextView)v.findViewById(R.id.name);
            addressView = (TextView)v.findViewById(R.id.address);
            this.BltListener = listener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
                if(BltListener != null && getAdapterPosition() != -1){
                    BltListener.OnItemClick(view, getAdapterPosition());
                }
        }
    }



}
