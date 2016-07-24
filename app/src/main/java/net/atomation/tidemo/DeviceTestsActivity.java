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
import net.atomation.atomationsdk.api.IHumidityTemperatureListener;
import net.atomation.atomationsdk.api.IIRTemperatureListener;
import net.atomation.atomationsdk.api.ISensorTag2Atom;
import net.atomation.atomationsdk.ble.ScanHelper;
import net.atomation.atomationsdk.ble.SensorTagAtomManager;

import java.util.Locale;

public class DeviceTestsActivity extends AppCompatActivity {

    private static final String TAG = DeviceTestsActivity.class.getSimpleName();

    public static final String INTENT_EXTRA_DEVICE_ADDRESS = "intent_extra_device_address";

    private ISensorTag2Atom atom;

    private Button btnConnect;
    private boolean isConnected;
    private TextView tvIsConnected;

    private Button btnReadIRTemp;
    private TextView tvIRTempReadTemp;
    private TextView tvIRTempReadAmbient;
    private Button btnIRNotifications;
    private boolean isListeningIRNotifications;
    private TextView tvIRNotificationsTemp;
    private TextView tvIRNotificationsAmbient;
    private Button btnIRStopNotifications;

    private IIRTemperatureListener irTemperatureListener = new IIRTemperatureListener() {

        @Override
        public void onIRTemperatureRead(final float temperature, final float ambient) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onIRNotificationsValuesChanged(temperature, ambient);
                }
            });
        }

        @Override
        public void onIRTemperatureReadError(int error) {

        }
    };

    private Button btnReadHumidTemp;
    private TextView tvHumidReadTemp;
    private TextView tvHumidReadHumid;
    private Button btnHumidNotifications;
    private TextView tvHumidNotificationsTemp;
    private TextView tvHumidNotificationsHumid;
    private Button btnHumidStopNotifications;

    private boolean isListeningHumidityNotifications;

    private IHumidityTemperatureListener humidityTemperatureListener = new IHumidityTemperatureListener() {
        @Override
        public void onChange(final double temperature, final double humidity) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onHumidityNotificationsValuesChanged(temperature, humidity);
                }
            });
        }

        @Override
        public void onError(int error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_tests);

        ScanHelper scanHelper = new ScanHelper(this);
        scanHelper.stopScanning();

        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(INTENT_EXTRA_DEVICE_ADDRESS);

        // first, get an instance of SensorTagAtomManager
        final SensorTagAtomManager atomManager = SensorTagAtomManager.getInstance(this);

        // then, test if the atom has already been created elsewhere
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

        setupEventSending();

        setupIRTemperatureSensor();

        setupHumidityTemperatureSensor();

        updateIsConnected();
    }

    private void setupEventSending() {
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

    private void setupHumidityTemperatureSensor() {
        btnReadHumidTemp = (Button) findViewById(R.id.btn_read_humidity);
        btnReadHumidTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atom.readHumidityTemperature(new IHumidityTemperatureListener() {
                    @Override
                    public void onChange(final double temperature, final double humidity) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onReadHumidityChange(temperature, humidity);
                            }
                        });
                    }

                    @Override
                    public void onError(int error) {

                    }
                });
            }
        });

        tvHumidReadTemp = (TextView) findViewById(R.id.tv_humidity_read_temp);
        tvHumidReadHumid = (TextView) findViewById(R.id.tv_humidity_read_humid);

        btnHumidNotifications = (Button) findViewById(R.id.btn_humid_notifications);
        btnHumidNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isListeningHumidityNotifications) {
                    stopListeningHumidityNotifications();
                } else {
                    startHumidityNotifications();
                }
            }
        });

        tvHumidNotificationsTemp = (TextView) findViewById(R.id.tv_humidity_notif_temp);
        tvHumidNotificationsHumid = (TextView) findViewById(R.id.tv_humidity_notif_humid);

        btnHumidStopNotifications = (Button) findViewById(R.id.btn_humid_stop_notifications);
        btnHumidStopNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopHumidNotifications();
            }
        });
    }

    private void setupIRTemperatureSensor() {
        btnReadIRTemp = (Button) findViewById(R.id.btn_read_ir_temp);
        btnReadIRTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atom.readIRTemperature(new IIRTemperatureListener() {

                    @Override
                    public void onIRTemperatureRead(final float temperature, final float ambient) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onReadIRTempChange(temperature, ambient);
                            }
                        });
                    }

                    @Override
                    public void onIRTemperatureReadError(int error) {

                    }
                });
            }
        });

        tvIRTempReadTemp = (TextView) findViewById(R.id.tv_ir_temp_read_temp);
        tvIRTempReadAmbient = (TextView) findViewById(R.id.tv_ir_temp_read_ambient);

        btnIRNotifications = (Button) findViewById(R.id.btn_ir_temp_notifications);
        btnIRNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isListeningIRNotifications) {
                    stopListeningIRNotifications();
                } else {
                    startIRNotifications();
                }
            }
        });

        tvIRNotificationsTemp = (TextView) findViewById(R.id.tv_ir_notif_temp);
        tvIRNotificationsAmbient = (TextView) findViewById(R.id.tv_ir_notif_ambient);

        btnIRStopNotifications = (Button) findViewById(R.id.btn_ir_stop_notifications);
        btnIRStopNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopIRNotifications();
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
        btnReadIRTemp.setEnabled(isConnected);
        btnIRNotifications.setEnabled(isConnected);
        btnReadHumidTemp.setEnabled(isConnected);
        btnHumidNotifications.setEnabled(isConnected);
    }

    private void disconnect() {
        atom.disconnect();
    }

    private void onReadIRTempChange(float temperature, float ambient) {
        tvIRTempReadTemp.setText(String.format(Locale.getDefault(), "%.2fc", temperature));
        tvIRTempReadAmbient.setText(String.format(Locale.getDefault(), "%.2f%%", ambient));
    }

    private void startIRNotifications() {
        isListeningIRNotifications = true;
        atom.startIRTemperatureNotifications(irTemperatureListener);
        btnIRNotifications.setText(R.string.btn_device_notification_stop_listening);
        btnIRStopNotifications.setVisibility(View.VISIBLE);
    }

    private void stopListeningIRNotifications() {
        isListeningIRNotifications = false;
        atom.stopListeningIRTemperature(irTemperatureListener);
        btnIRNotifications.setText(R.string.btn_device_notification_start);
    }

    private void onIRNotificationsValuesChanged(float temperature, float ambient) {
        tvIRNotificationsTemp.setText(String.format(Locale.getDefault(), "%.2fc", temperature));
        tvIRNotificationsAmbient.setText(String.format(Locale.getDefault(), "%.2f%%", ambient));
    }

    private void stopIRNotifications() {
        atom.stopIRTemperatureNotifications();
        btnIRStopNotifications.setVisibility(View.INVISIBLE);
    }

    private void onReadHumidityChange(double temperature, double humidity) {
        tvHumidReadTemp.setText(String.format(Locale.getDefault(), "%.2fc", temperature));
        tvHumidReadHumid.setText(String.format(Locale.getDefault(), "%.2f%%", humidity));
    }

    private void startHumidityNotifications() {
        isListeningHumidityNotifications = true;
        atom.startHumidityTemperatureNotifications(humidityTemperatureListener);
        btnHumidNotifications.setText(R.string.btn_device_notification_stop_listening);
        btnHumidStopNotifications.setVisibility(View.VISIBLE);
    }

    private void stopListeningHumidityNotifications() {
        isListeningHumidityNotifications = false;
        atom.stopListeningHumidityTemperature(humidityTemperatureListener);
        btnHumidNotifications.setText(R.string.btn_device_notification_start);
    }

    private void onHumidityNotificationsValuesChanged(double temperature, double humidity) {
        tvHumidNotificationsTemp.setText(String.format(Locale.getDefault(), "%.2fc", temperature));
        tvHumidNotificationsHumid.setText(String.format(Locale.getDefault(), "%.2f%%", humidity));
    }

    private void stopHumidNotifications() {
        atom.stopHumidityTemperatureNotifications();
        btnHumidStopNotifications.setVisibility(View.INVISIBLE);
    }
}
