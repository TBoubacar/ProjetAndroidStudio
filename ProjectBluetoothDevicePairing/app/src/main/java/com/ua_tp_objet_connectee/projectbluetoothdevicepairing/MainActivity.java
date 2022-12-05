package com.ua_tp_objet_connectee.projectbluetoothdevicepairing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
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

import com.ua_tp_objet_connectee.projectbluetoothdevicepairing.adapter.MyListViewAdapter;
import com.ua_tp_objet_connectee.projectbluetoothdevicepairing.model.MyBluetoothGattCallback;
import com.ua_tp_objet_connectee.projectbluetoothdevicepairing.model.MyListViewItem;

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
    private MyBluetoothGattCallback myBluetoothGattCallback;
    private BluetoothGatt bluetoothGattConnected;
    private MyListViewItem bluetoothInfo;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MY_LOGGER.info("________{onCreate}________");

        ListView bluetoothList = findViewById(R.id.bluetoothListView);

        activatedBluetoothScanningBtn = findViewById(R.id.start_btn);
        unactivatedBluetoothScanningBtn = findViewById(R.id.stop_btn);
        disconnect_btn = findViewById(R.id.disconnect_btn);
        deviceConnectedName = findViewById(R.id.deviceConnectedId);
        deviceConnectedInfo = findViewById(R.id.deviceConnectedInfoId);

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
                    MY_LOGGER.info("");
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
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    MY_LOGGER.info("");
                }
                Bundle bundle = msg.getData();
                bluetoothInfo.setName(bundle.getString("deviceName"));
                String disconnectedBtn = bundle.getString("disconnectedBtn");
                String infoGattService = bundle.getString("infoGattService");
                if (bluetoothGattConnected != null && bluetoothGattConnected.connect()) {
                    disconnect_btn.setEnabled(true);
                    deviceConnectedName.setText(bluetoothInfo.getName());
                    deviceConnectedInfo.setText(infoGattService);
                }
                if (disconnectedBtn != null && disconnectedBtn.equals("false")) {
                    disconnect_btn.setEnabled(false);
                    deviceConnectedName.setText(bluetoothInfo.getName());
                    deviceConnectedInfo.setText(infoGattService);
                }

            }
        };
        myBluetoothGattCallback = new MyBluetoothGattCallback(this);
        this.scanning = false;
        this.bluetoothInfo = new MyListViewItem("","");
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
        this.disconnect();
        this.close();
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
            this.scanLeDevice(this);
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
        TextView bluetoothItemAddress = view.findViewById(R.id.bluetoothItemAddress);
        this.bluetoothInfo.setAddress((String) bluetoothItemAddress.getText());
        this.connect(this.bluetoothInfo.getAddress());
    }

    public void actionToDoWhenDeviceIsDisconnected() {
        disconnect_btn.setEnabled(false);
        deviceConnectedName.setText(null);
        deviceConnectedInfo.setText(null);
        this.disconnect();

        String msg = "_________Bluetooth Device (" + this.bluetoothInfo.getName() + ") is disconnected successfully_________";
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
                        MY_LOGGER.info("");
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
                    MY_LOGGER.info("");
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

    public void connect(final String deviceAddress) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            MY_LOGGER.info("We Do Nothing");
        }
        bluetoothGattConnected = device.connectGatt(this.getApplicationContext(), false, myBluetoothGattCallback);
    }

    public void disconnect() {
        if (bluetoothGattConnected == null) {
            MY_LOGGER.info("__________BluetoothDevice is not initialized for being disconnect__________");
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            MY_LOGGER.info("We Do Nothing");
        }
        bluetoothGattConnected.disconnect();
        bluetoothGattConnected = null;
    }

    public void close() {
        if (bluetoothGattConnected == null) {
            String msg = "_________Impossible to close The GATT Server Connection_________";
            MY_LOGGER.info(msg);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            MY_LOGGER.info("We Do Nothing");
        }
        bluetoothGattConnected.close();
        String msg = "_________The GATT Server Connection Is Closed_________";
        MY_LOGGER.info(msg);
    }

    public Handler getHandler() {
        return handler;
    }

    public BluetoothGatt getBluetoothGattConnected() {
        return bluetoothGattConnected;
    }
}