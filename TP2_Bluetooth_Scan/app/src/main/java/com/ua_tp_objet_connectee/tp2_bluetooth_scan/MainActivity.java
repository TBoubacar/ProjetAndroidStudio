package com.ua_tp_objet_connectee.tp2_bluetooth_scan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ua_tp_objet_connectee.tp2_bluetooth_scan.adapter.MyListViewAdapter;
import com.ua_tp_objet_connectee.tp2_bluetooth_scan.model.MyListViewItem;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static Logger MY_LOGGER = Logger.getLogger(MainActivity.class.getName());
    private final Context mainActivityContext = this;

    private static final int REQUEST_ENABLE_BT = 1;
    private static MyListViewAdapter myListViewAdapter;
    private static Button actifBluetooth;
    private static Button unactifBluetooth;
    private BluetoothAdapter bluetoothAdapter;
    private ListView bluetoothList;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner bluetoothLeScanner;
    // Device scan callback.
    private ScanCallback leScanCallback;
    private Handler handler;
    private boolean scanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MY_LOGGER.info("________{onCreate}________");

        actifBluetooth = (Button) findViewById(R.id.start_btn);
        unactifBluetooth = (Button) findViewById(R.id.stop_btn);

        actifBluetooth.setOnClickListener(view -> actionToDoWhenBluetoothIsActif());
        unactifBluetooth.setOnClickListener(view -> actionToDoWhenBluetoothIsUnactif());

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            String msg = "________User active bluetooth!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
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