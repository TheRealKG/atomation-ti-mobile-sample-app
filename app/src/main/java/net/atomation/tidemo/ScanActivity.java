package net.atomation.tidemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.atomation.atomationsdk.api.IScanListener;
import net.atomation.atomationsdk.ble.ScanHelper;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = ScanActivity.class.getSimpleName();

    private Button btnScan;
    private ScanHelper scanHelper;
    private boolean isScanning = false;
    private ScanAdapter scanAdapter;
    private TextView tvIsScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scanHelper = new ScanHelper(this);

        tvIsScanning = (TextView) findViewById(R.id.tv_scan_is_scanning);

        RecyclerView rcvScanDevices = (RecyclerView) findViewById(R.id.rcv_scan_devices);
        rcvScanDevices.setLayoutManager(new LinearLayoutManager(this));
        scanAdapter = new ScanAdapter();
        rcvScanDevices.setAdapter(scanAdapter);

        btnScan = (Button) findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScanning) {
                    stopScan();
                } else {
                    StartScan();
                }
            }
        });
    }

    private void StartScan() {
        scanAdapter.clear();
        IScanListener listener = new IScanListener() {
            @Override
            public void onDeviceFound(final String deviceAddress, final String deviceName, int rssi, byte[] scanRecord) {
                // onDeviceFound always runs off the UI thread! can't update the UI from it.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scanAdapter.addDevice(deviceName, deviceAddress);
                    }
                });
            }

            @Override
            public void onScanStatus(final boolean isScanning) {
                // onScanStatus always runs off the UI thread! can't update the UI from it.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateScanStatus(isScanning);
                    }
                });
            }

            @Override
            public void onScanError(int errorCode) {
                Log.e(TAG, "scan error: " + errorCode);
            }
        };

        scanHelper.startScanning(listener);
        btnScan.setText(R.string.btn_scan_stop_text);
    }

    private void updateScanStatus(boolean isScanning) {
        if (this.isScanning != isScanning) {
            this.isScanning = isScanning;
            btnScan.setText(isScanning ? R.string.btn_scan_stop_text : R.string.btn_scan_start_text);
            tvIsScanning.setText(String.valueOf(isScanning));
        }
    }

    private void stopScan() {
        scanHelper.stopScanning();
        btnScan.setText(R.string.btn_scan_start_text);
    }
}
