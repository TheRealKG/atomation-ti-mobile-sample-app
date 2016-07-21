package net.atomation.tidemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.atomation.atomationsdk.api.IConnectionStateListener;
import net.atomation.atomationsdk.api.ISensorTag2Atom;
import net.atomation.atomationsdk.ble.SensorTagAtomManager;

public class DeviceTestsActivity extends AppCompatActivity {

    private static final String TAG = DeviceTestsActivity.class.getSimpleName();

    public static final String INTENT_EXTRA_DEVICE_ADDRESS = "intent_extra_device_address";

    private ISensorTag2Atom atom;
    private Button btnConnect;
    private boolean isConnected;
    private TextView tvIsConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_tests);

        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(INTENT_EXTRA_DEVICE_ADDRESS);

        // first, get an instance of SensorTagAtomManager
        SensorTagAtomManager atomManager = SensorTagAtomManager.getInstance(this);

        // then, test if the atom has already been created beforehand
        // getSensorTagDevice will return null if the atom wasn't found
        atom = atomManager.getSensorTag2Device(deviceAddress);

        if (atom == null) {
            // An atom with this MAC address has never been created, need to create a new one
            // createSensorTag2Device will return null if an atom with the same MAC address has been created previously,
            // but this was checked for above
            atom = atomManager.createSensorTag2Device(deviceAddress);
        }

        TextView tvTitle = (TextView) findViewById(R.id.tv_tests_title);
        tvTitle.setText(deviceAddress);

        btnConnect = (Button) findViewById(R.id.btn_tests_connect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected) {
                    disconnect();
                } else {
                    connect();
                }
            }
        });

        tvIsConnected = (TextView) findViewById(R.id.tv_tests_is_connected);
        updateIsConnected();

        final EditText edtEventName = (EditText) findViewById(R.id.edt_events_event_name);
        final EditText edtEventType = (EditText) findViewById(R.id.edt_events_event_type);
        final EditText edtEventData = (EditText) findViewById(R.id.edt_events_event_data);

        Button btnSendEvent = (Button) findViewById(R.id.btn_send_event);
        btnSendEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eventName = edtEventName.getText().toString();
                String eventType = edtEventType.getText().toString();
                String eventData = edtEventData.getText().toString();
                Log.d(TAG, String.format("send event: name - %s, type - %s, data - %s", eventName, eventType, eventData));
                atom.sendEvent(eventName, eventType, eventData);
            }
        });

    }

    private void connect() {
        atom.connect(new IConnectionStateListener() {
            @Override
            public void onDeviceConnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DeviceTestsActivity.this.onDeviceConnected();
                    }
                });
            }

            @Override
            public void onDeviceDisconnect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onDeviceDisconnected();
                    }
                });
            }

            @Override
            public void onConnectionError(int error) {

            }
        });
    }

    private void onDeviceDisconnected() {
        isConnected = false;
        btnConnect.setText(R.string.btn_connect_text);
        updateIsConnected();
    }

    private void onDeviceConnected() {
        isConnected = true;
        btnConnect.setText(R.string.btn_disconnect_text);
        updateIsConnected();
    }

    private void updateIsConnected() {
        tvIsConnected.setText(String.valueOf(isConnected));
    }

    private void disconnect() {
        atom.disconnect();
    }
}
