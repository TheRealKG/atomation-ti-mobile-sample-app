package net.atomation.tidemo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for the found devices RecyclerView
 */
public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {

    private static final String TAG = ScanAdapter.class.getSimpleName();

    private final List<Device> mDataSet = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device device = mDataSet.get(position);
        holder.tvDeviceName.setText(device.name);
        holder.tvDeviceAddress.setText(device.macAddress);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @UiThread
    public synchronized void addDevice(String name, String mac) {
        if (!deviceAlreadyExist(mac)) {
            mDataSet.add(new Device(name, mac));
            notifyDataSetChanged();
        }

    }

    @UiThread
    public synchronized void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    private boolean deviceAlreadyExist(String mac) {
        boolean exists = false;
        for (Device device : mDataSet) {
            if (device.macAddress.equals(mac)) {
                exists = true;
                break;
            }
        }

        return exists;
    }

    private void startTestActivity(Context context, String address) {
        Log.d(TAG, "starting to test: " + address);
        Intent intent = new Intent(context, DeviceTestsActivity.class);
        intent.putExtra(DeviceTestsActivity.INTENT_EXTRA_DEVICE_ADDRESS, address);
        context.startActivity(intent);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDeviceName;
        private final TextView tvDeviceAddress;

        public ViewHolder(View itemView) {
            super(itemView);

            tvDeviceName = (TextView) itemView.findViewById(R.id.scan_list_item_name);
            tvDeviceAddress = (TextView) itemView.findViewById(R.id.scan_list_item_address);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startTestActivity(view.getContext(), tvDeviceAddress.getText().toString());
                }
            });
        }
    }

    private static class Device {
        final String name;
        final String macAddress;

        public Device(String name, String macAddress) {
            this.name = name;
            this.macAddress = macAddress;
        }
    }
}
