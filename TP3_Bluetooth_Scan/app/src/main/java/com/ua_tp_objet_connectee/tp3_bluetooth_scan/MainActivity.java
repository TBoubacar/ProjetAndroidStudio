package com.ua_tp_objet_connectee.tp3_bluetooth_scan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ua_tp_objet_connectee.tp3_bluetooth_scan.adapter.MyListViewAdapter;
import com.ua_tp_objet_connectee.tp3_bluetooth_scan.model.BluetoothLeService;
import com.ua_tp_objet_connectee.tp3_bluetooth_scan.model.MyListViewItem;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static Logger MY_LOGGER = Logger.getLogger(MainActivity.class.getName());
    private final Context mainActivityContext = this;
    private static final int REQUEST_ENABLE_BT = 1;
    private static MyListViewAdapter myListViewAdapter;
    private static Button actifBluetooth;
    private static Button unactifBluetooth;
    public static Button disconnect_btn;
    public static TextView deviceConnectedName;
    public static TextView deviceConnectedInfo;

    private BluetoothAdapter bluetoothAdapter;
    private ListView bluetoothList;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner bluetoothLeScanner;
    // Device scan callback.
    private ScanCallback leScanCallback;
    private Handler handler;
    private boolean scanning;
    // Connexio to the server GATT
    private BluetoothLeService bluetoothService;
    private ServiceConnection serviceConnection;
    private String deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MY_LOGGER.info("________{onCreate}________");

        actifBluetooth = (Button) findViewById(R.id.start_btn);
        unactifBluetooth = (Button) findViewById(R.id.stop_btn);
        disconnect_btn = (Button) findViewById(R.id.disconnect_btn);
        deviceConnectedName = (TextView) findViewById(R.id.deviceConnectedId);
        deviceConnectedInfo = (TextView) findViewById(R.id.deviceConnectedInfoId);

        actifBluetooth.setOnClickListener(view -> actionToDoWhenBluetoothIsActif());
        unactifBluetooth.setOnClickListener(view -> actionToDoWhenBluetoothIsUnactif());
        disconnect_btn.setOnClickListener(view -> actionToDoWhenDeviceIsDisconnected());

        myListViewAdapter = new MyListViewAdapter(this);
        bluetoothList = findViewById(R.id.bluetoothListView);
        bluetoothList.setAdapter(myListViewAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (ActivityCompat.checkSelfPermission(mainActivityContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                }
                String name = result.getDevice().getName();
                String address = result.getDevice().getAddress();
                if (name == null) {
                    name = "Null Name";
                }
                if (address == null) {
                    address = "Null Address";
                }
                myListViewAdapter.addItem(new MyListViewItem(name, address));
            }
        };
        handler = new Handler();
        scanning = false;

        bluetoothService = new BluetoothLeService(this);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                String msg = "__________Bluetooth Service is started__________";
                MY_LOGGER.info(msg);

                bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!bluetoothService.initialize()) {
                    msg = "__________Bluetooth Service is not initialize__________";
                    MY_LOGGER.info(msg);
                    Toast.makeText(mainActivityContext, msg, Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (bluetoothService != null) {
                    Boolean status = bluetoothService.connect(deviceAddress);
                    if (status) {
                        msg = "__________Bluetooth Device is connected__________";
                        MY_LOGGER.info(msg);
                    } else {
                        msg = "__________Bluetooth Device is not connected with provided address (" + deviceAddress + ")__________";
                        MY_LOGGER.info(msg);
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bluetoothService.disconnect();
                String msg = "__________Bluetooth Service is ended__________";
                MY_LOGGER.info(msg);
            }
        };
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
        bluetoothService.disconnect();
        bluetoothService.close();
        System.exit(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            String msg = "________User active bluetooth!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            finish();
        }
    }

    public void actionToDoWhenBluetoothIsActif() {
        actifBluetooth.setEnabled(false);
        unactifBluetooth.setEnabled(true);
        scanning = false;

        if (bluetoothAdapter == null) {
            String msg = "________The bluetooth device can not be connected!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        } else {
            this.scanLeDevice(this);
        }
    }

    public void actionToDoWhenBluetoothIsUnactif() {
        scanning = true;
        unactifBluetooth.setEnabled(false);
        actifBluetooth.setEnabled(true);

        if (bluetoothAdapter == null) {
            String msg = "________The bluetooth device can not be connected!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        } else {
            this.scanLeDevice(this);
        }
    }

    public void actionToDoWhenDeviceIsConnected(View view) {
        final Intent intentFilter = new Intent(String.valueOf(BluetoothGatt.STATE_CONNECTED));
        TextView bluetoothItemAddress = (TextView) view.findViewById(R.id.bluetoothItemAdress);

        deviceAddress = (String) bluetoothItemAddress.getText();
        serviceConnection.onServiceConnected(ComponentName.unflattenFromString(""), bluetoothService.onBind(intentFilter));
    }

    public void actionToDoWhenDeviceIsDisconnected() {
        serviceConnection.onServiceDisconnected(ComponentName.unflattenFromString(""));
    }

    private void scanLeDevice(Context context) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, REQUEST_ENABLE_BT);
        } else {
            if (!scanning) {
                // Stops scanning after a predefined scan period.
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                        }
                        scanning = false;
                        bluetoothLeScanner.stopScan(leScanCallback);
                        actifBluetooth.setEnabled(true);
                        unactifBluetooth.setEnabled(false);
                    }
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
                }
                actifBluetooth.setEnabled(false);
                unactifBluetooth.setEnabled(true);
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

}