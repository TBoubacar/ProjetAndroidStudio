package com.ua_tp_objet_connectee.tp3_bluetooth_scan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ua_tp_objet_connectee.tp3_bluetooth_scan.adapter.MyListViewAdapter;
import com.ua_tp_objet_connectee.tp3_bluetooth_scan.model.BluetoothGattCallback;
import com.ua_tp_objet_connectee.tp3_bluetooth_scan.model.MyListViewItem;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    //  MainActivity Class property : Use for display information and help to connect a bluetooth in devices
    private static final Logger MY_LOGGER = Logger.getLogger(MainActivity.class.getName());
    private static final int REQUEST_ENABLE_BT = 1;

    //  Property : Use for Adapter
    private MyListViewAdapter myListViewAdapter;
    private BluetoothAdapter bluetoothAdapter;

    //  Property : Use for select and change information on my graphic interface
    private TextView deviceConnectedName;
    private TextView deviceConnectedInfo;
    private Button unactivatedBluetoothScanningBtn;
    private Button activatedBluetoothScanningBtn;
    private Button disconnect_btn;


    // Property : Use for stops scanning after 10 seconds
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner bluetoothLeScanner;

    // Property : Use for device scan callback
    private ScanCallback leScanCallback;
    private boolean scanning;
    private Handler handler;

    // Property : Use for Connection to the server GATT
    private BluetoothGattCallback bluetoothGattCallback;
    private String deviceName;
    private boolean connected;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MY_LOGGER.info("________{onCreate}________");

        ListView bluetoothList = findViewById(R.id.bluetoothListView);

        activatedBluetoothScanningBtn = (Button) findViewById(R.id.start_btn);
        unactivatedBluetoothScanningBtn = (Button) findViewById(R.id.stop_btn);
        disconnect_btn = (Button) findViewById(R.id.disconnect_btn);
        deviceConnectedName = (TextView) findViewById(R.id.deviceConnectedId);
        deviceConnectedInfo = (TextView) findViewById(R.id.deviceConnectedInfoId);

        activatedBluetoothScanningBtn.setOnClickListener(view -> actionToDoToStartScanning());
        unactivatedBluetoothScanningBtn.setOnClickListener(view -> actionToDoToStopScanning());
        disconnect_btn.setOnClickListener(view -> actionToDoWhenDeviceIsDisconnected());

        myListViewAdapter = new MyListViewAdapter(this);
        bluetoothList.setAdapter(myListViewAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    MY_LOGGER.info("We Do Nothing");
                }
                String name = result.getDevice().getName();
                String address = result.getDevice().getAddress();
                if (name == null) {
                    name = "Null Device Name";
                }
                if (address == null) {
                    address = "Null Device Address";
                }
                myListViewAdapter.addItem(new MyListViewItem(name, address));
            }
        };

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                Bundle bundle = msg.getData();
                String disconnect_btn_bool = bundle.getString("disconnect_btn");
                String infoGattService = bundle.getString("infoGattService");
                deviceName = bundle.getString("deviceName");

                if (disconnect_btn_bool.equals("true")) {
                    disconnect_btn.setEnabled(true);
                    deviceConnectedName.setText(deviceName);
                    deviceConnectedInfo.setText(infoGattService);
                }

            }
        };
        scanning = false;
        connected = false;
        bluetoothGattCallback = new BluetoothGattCallback(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MY_LOGGER.info("________{onStart}________");
    }

    @Override
    protected void onStop() {
        super.onStop();
        MY_LOGGER.info("________{onStop}________");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothGattCallback.disconnect();
        bluetoothGattCallback.close();
        System.exit(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //  Recovering the bluetooth start request on the device
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            String msg = "________User active bluetooth!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            finish();
        }
    }

    public void actionToDoToStartScanning() {
        activatedBluetoothScanningBtn.setEnabled(false);
        unactivatedBluetoothScanningBtn.setEnabled(true);
        scanning = false;

        if (bluetoothAdapter == null) {
            String msg = "________The bluetooth device can not be connected!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        this.scanLeDevice(this);
    }

    public void actionToDoToStopScanning() {
        scanning = true;
        unactivatedBluetoothScanningBtn.setEnabled(false);
        activatedBluetoothScanningBtn.setEnabled(true);

        if (bluetoothAdapter == null) {
            String msg = "________The bluetooth device can not be connected!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        this.scanLeDevice(this);
    }

    public void actionToDoWhenDeviceIsConnected(View view) {
        TextView bluetoothItemAddress = (TextView) view.findViewById(R.id.bluetoothItemAddress);
        String deviceAddress = (String) bluetoothItemAddress.getText();

        if (!bluetoothGattCallback.initialize()) {
            finish();
        }
        if (bluetoothGattCallback != null) {
            this.connected = bluetoothGattCallback.connect(deviceAddress);
            if (this.connected) {
                String msg = "_________Bluetooth Device (" + deviceName + ") is connected successfully_________";
                MY_LOGGER.info(msg);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void actionToDoWhenDeviceIsDisconnected() {
        disconnect_btn.setEnabled(false);
        deviceConnectedName.setText(null);
        deviceConnectedInfo.setText(null);
        bluetoothGattCallback.disconnect();

        String msg = "_________Bluetooth Device (" + deviceName + ") is disconnected successfully_________";
        MY_LOGGER.info(msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void scanLeDevice(Context context) {
        if (!bluetoothAdapter.isEnabled()) {
            //  If the user has not activated bluetooth on his machine
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, REQUEST_ENABLE_BT);
        } else {
            if (!scanning) {
                // Stops scanning after a predefined scan period.
                handler.postDelayed(() -> {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        MY_LOGGER.info("We Do Nothing");
                    }
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    activatedBluetoothScanningBtn.setEnabled(true);
                    unactivatedBluetoothScanningBtn.setEnabled(false);
                }, SCAN_PERIOD);

                scanning = true;
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    MY_LOGGER.info("We Do Nothing");
                }
                activatedBluetoothScanningBtn.setEnabled(false);
                unactivatedBluetoothScanningBtn.setEnabled(true);
                bluetoothLeScanner.startScan(leScanCallback);

                String msg = "________The bluetooth device start scanning!________";
                MY_LOGGER.info(msg);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            } else {
                scanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);

                String msg = "________The bluetooth device stop scanning!________";
                MY_LOGGER.info(msg);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

}