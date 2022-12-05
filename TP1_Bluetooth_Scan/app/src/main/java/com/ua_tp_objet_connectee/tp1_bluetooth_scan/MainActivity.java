package com.ua_tp_objet_connectee.tp1_bluetooth_scan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ua_tp_objet_connectee.tp1_bluetooth_scan.adapter.MyListViewAdapter;
import com.ua_tp_objet_connectee.tp1_bluetooth_scan.model.MyListViewItem;

import java.util.Set;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static Logger MY_LOGGER = Logger.getLogger(MainActivity.class.getName());

    private static final int REQUEST_ENABLE_BT = 1;
    private static MyListViewAdapter myListViewAdapter;
    private static Button actifBluetooth;
    private static Button unactifBluetooth;
    private BluetoothAdapter bluetoothAdapter;
    private MyBroadCastReceiver broadcastReceiver;
    private ListView bluetoothList;

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
        broadcastReceiver = new MyBroadCastReceiver();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        if (devices != null && devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                myListViewAdapter.addItem(new MyListViewItem(device.getName(), device.getAddress()));
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            String msg = "________User active bluetooth!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter intentFilterOfChange = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        IntentFilter intentFilterOfPairing = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        IntentFilter intentFilterOfStartDiscovery = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter intentFilterOfFinishDiscovery = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(broadcastReceiver, intentFilter);
        this.registerReceiver(broadcastReceiver, intentFilterOfChange);
        this.registerReceiver(broadcastReceiver, intentFilterOfPairing);
        this.registerReceiver(broadcastReceiver, intentFilterOfStartDiscovery);
        this.registerReceiver(broadcastReceiver, intentFilterOfFinishDiscovery);

        MY_LOGGER.info("________{onStart}________");
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(this.broadcastReceiver);

        MY_LOGGER.info("________{onStop}________");
    }

    public void actionToDoWhenBluetoothIsActif() {
        actifBluetooth.setEnabled(false);
        unactifBluetooth.setEnabled(true);

        if (bluetoothAdapter == null) {
            String msg = "________The bluetooth device can not be connected!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, REQUEST_ENABLE_BT);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            if (! bluetoothAdapter.isDiscovering()) {
                String msg = "________The bluetooth device start discovering successfuly!________";
                MY_LOGGER.info(msg);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                bluetoothAdapter.startDiscovery();
            }
        }

    }

    public void actionToDoWhenBluetoothIsUnactif() {
        unactifBluetooth.setEnabled(false);
        actifBluetooth.setEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        if(bluetoothAdapter.isDiscovering()) {
            String msg = "________The bluetooth device stop discovering successfuly!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            bluetoothAdapter.cancelDiscovery();
        }
    }




    /*__________________________CLASS EXTERN__________________________*/
    public static class MyBroadCastReceiver extends BroadcastReceiver {
        public MyBroadCastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Action : " + intent.getAction() + "\n");
            stringBuilder.append("URI : " + intent.toUri(Intent.URI_INTENT_SCHEME));
            stringBuilder.append("\n_______________________\n");

            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction()) || BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction()) ) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                }
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String name = device.getName();
                    String address = device.getAddress();
                    if (name == null) {
                        name = "Null Name";
                    }
                    if (address == null) {
                        address = "Null Address";
                    }
                    myListViewAdapter.addItem(new MyListViewItem(name, address));
                }
            }
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction())) {
                String msg = "________The bluetooth is pairing successfuly!________";
                MY_LOGGER.info(msg);

            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                String msg = "________The bluetooth is finished discovery successfuly!________";
                MY_LOGGER.info(msg);
                MainActivity.actifBluetooth.setEnabled(true);
                MainActivity.unactifBluetooth.setEnabled(false);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())) {
                String msg = "________The bluetooth is started discovery successfuly!________";
                MY_LOGGER.info(msg);
                MainActivity.actifBluetooth.setEnabled(false);
                MainActivity.unactifBluetooth.setEnabled(true);
            }
        }
    }

}
