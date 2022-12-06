package com.ua_tp_objet_connectee.projectbluetoothdevicepairing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ua_tp_objet_connectee.projectbluetoothdevicepairing.adapter.MyListViewAdapter;
import com.ua_tp_objet_connectee.projectbluetoothdevicepairing.model.MyBluetoothGattCallback;
import com.ua_tp_objet_connectee.projectbluetoothdevicepairing.model.MyListViewItem;

import java.util.Set;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    //  MainActivity Class property : Use for display information and help to connect a bluetooth in devices
    private static final Logger MY_LOGGER = Logger.getLogger(MainActivity.class.getName());
    private static final int REQUEST_ENABLE_BT = 1;

    //  Property : Use for Adapter
    @SuppressLint("StaticFieldLeak")
    private static MyListViewAdapter myListViewAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private MyBroadCastReceiver broadcastReceiver;

    //  Property : Use for select and change information on my graphic interface
    private TextView deviceConnectedName;
    private TextView deviceConnectedInfo;
    @SuppressLint("StaticFieldLeak")
    private static Button unactivatedBluetoothScanningBtn;
    @SuppressLint("StaticFieldLeak")
    private static Button activatedBluetoothScanningBtn;
    private Button disconnect_btn;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch bluetoothType4_0;


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

        activatedBluetoothScanningBtn = findViewById(R.id.start_btn);
        unactivatedBluetoothScanningBtn = findViewById(R.id.stop_btn);
        disconnect_btn = findViewById(R.id.disconnect_btn);
        deviceConnectedName = findViewById(R.id.deviceConnectedId);
        deviceConnectedInfo = findViewById(R.id.deviceConnectedInfoId);
        bluetoothType4_0 = findViewById(R.id.bluetoothTypeId);

        activatedBluetoothScanningBtn.setOnClickListener(view -> actionToDoToStartScanningForBluetooth4_0());
        unactivatedBluetoothScanningBtn.setOnClickListener(view -> actionToDoToStopScanningForBluetooth4_0());
        disconnect_btn.setOnClickListener(view -> actionToDoWhenDeviceIsDisconnected());
        bluetoothType4_0.setOnClickListener(view -> actionToDoToWhenChangeTypeOfBluetooth());

        myListViewAdapter = new MyListViewAdapter(this);
        ListView bluetoothList = findViewById(R.id.bluetoothListView);
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
            MY_LOGGER.info("");
        }
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        if (devices != null && devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                myListViewAdapter.addItem(new MyListViewItem(device.getName(), device.getAddress()));
            }
        }

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
                MainActivity.myListViewAdapter.addItem(new MyListViewItem(name, address));
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

                    String msgInfo = "________Bluetooth Device '" + bluetoothInfo.getName() + "' Is Connected Successfully!________";
                    MY_LOGGER.info(msgInfo);
                    Toast.makeText(MainActivity.this, msgInfo, Toast.LENGTH_LONG).show();
                }
                if (disconnectedBtn != null && disconnectedBtn.equals("false")) {
                    disconnect_btn.setEnabled(false);
                    deviceConnectedName.setText(null);
                    deviceConnectedInfo.setText(null);

                    String msgInfo = "________Connection To Device '" + bluetoothInfo.getName() + "' Is stopped!________";
                    MY_LOGGER.info(msgInfo);
                    Toast.makeText(MainActivity.this, msgInfo, Toast.LENGTH_LONG).show();
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
        //  TIP : To remove all reference if there are
        if (this.broadcastReceiver != null) {
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(broadcastReceiver, intentFilter);
            this.unregisterReceiver(broadcastReceiver);
        }
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
            String msg = "________User Active Bluetooth In Device________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            this.scanLeDevice(this);
        }
    }

    public void actionToDoToStartScanningForBluetooth2_0() {
        activatedBluetoothScanningBtn.setEnabled(false);
        unactivatedBluetoothScanningBtn.setEnabled(true);

        if (bluetoothAdapter == null) {
            String msg = "________Bluetooth Is Not Used________";
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
                MY_LOGGER.info("");
            }
            if (! bluetoothAdapter.isDiscovering()) {
                String msg = "________Bluetooth Device Start Scanning With Bluetooth 2.0 Successfully!________";
                MY_LOGGER.info(msg);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                bluetoothAdapter.startDiscovery();
            }
        }

    }

    public void actionToDoToStopScanningForBluetooth2_0() {
        unactivatedBluetoothScanningBtn.setEnabled(false);
        activatedBluetoothScanningBtn.setEnabled(true);

        if (bluetoothAdapter == null) {
            String msg = "________Bluetooth Is Not Used________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            MY_LOGGER.info("");
        }
        if (bluetoothAdapter.isDiscovering()) {
            String msg = "________Bluetooth Device Stop Scanning With Bluetooth 2.0 Successfully!________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            bluetoothAdapter.cancelDiscovery();
        }
    }

    public void actionToDoToWhenChangeTypeOfBluetooth() {
        if (bluetoothType4_0 != null) {
            if (bluetoothType4_0.isChecked()) {
                this.unregisterReceiver(this.broadcastReceiver);
                activatedBluetoothScanningBtn.setOnClickListener(view -> actionToDoToStartScanningForBluetooth4_0());
                unactivatedBluetoothScanningBtn.setOnClickListener(view -> actionToDoToStopScanningForBluetooth4_0());
            } else {
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

                activatedBluetoothScanningBtn.setOnClickListener(view -> actionToDoToStartScanningForBluetooth2_0());
                unactivatedBluetoothScanningBtn.setOnClickListener(view -> actionToDoToStopScanningForBluetooth2_0());
            }
        } else {
            MY_LOGGER.warning("______________Bluetooth Type Is Not Defined!______________");
        }
    }

    public void actionToDoToStartScanningForBluetooth4_0() {
        activatedBluetoothScanningBtn.setEnabled(false);
        unactivatedBluetoothScanningBtn.setEnabled(true);
        scanning = false;

        if (bluetoothAdapter == null) {
            String msg = "________Bluetooth Is Not Used________";
            MY_LOGGER.info(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        this.scanLeDevice(this);
    }

    public void actionToDoToStopScanningForBluetooth4_0() {
        scanning = true;
        unactivatedBluetoothScanningBtn.setEnabled(false);
        activatedBluetoothScanningBtn.setEnabled(true);

        if (bluetoothAdapter == null) {
            String msg = "________Bluetooth Is Not Used________";
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

        String msg = "_________Bluetooth Device '" + this.bluetoothInfo.getName() + "' Is Disconnected Successfully_________";
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

                String msg = "________Bluetooth Device Start Scanning With Bluetooth 4.0________";
                MY_LOGGER.info(msg);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            } else {
                scanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);

                String msg = "________Bluetooth Device Stop Scanning With Bluetooth 4.0________";
                MY_LOGGER.info(msg);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
            MY_LOGGER.info("");
        }
        bluetoothGattConnected = device.connectGatt(this.getApplicationContext(), false, myBluetoothGattCallback);
    }

    public void disconnect() {
        if (bluetoothGattConnected == null) {
            MY_LOGGER.info("__________Bluetooth Device is not initialized for being disconnect__________");
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
            MY_LOGGER.info("");
        }
        bluetoothGattConnected.disconnect();
        bluetoothGattConnected = null;
    }

    public void close() {
        if (bluetoothGattConnected == null) {
            String msg = "_________Impossible To Close The GATT Server Connection_________";
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
            MY_LOGGER.info("");
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



    /*__________________________CLASS EXTERN__________________________*/
    public static class MyBroadCastReceiver extends BroadcastReceiver {
        public MyBroadCastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Action : ").append(intent.getAction()).append("\n");
            stringBuilder.append("URI : ").append(intent.toUri(Intent.URI_INTENT_SCHEME));
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
                    MY_LOGGER.info("");
                }
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String name = device.getName();
                    String address = device.getAddress();
                    if (name == null) {
                        name = "Null Device Name with bluetooth 2.0";
                    }
                    if (address == null) {
                        address = "Null Device Address with bluetooth 2.0";
                    }
                    MainActivity.myListViewAdapter.addItem(new MyListViewItem(name, address));
                }
            }
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction())) {
                String msg = "________Bluetooth Is Pairing Successfully With Bluetooth 2.0________";
                MY_LOGGER.info(msg);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())) {
                String msg = "________Bluetooth Device Start Scanning With Bluetooth 2.0________";
                MY_LOGGER.info(msg);
                MainActivity.activatedBluetoothScanningBtn.setEnabled(false);
                MainActivity.unactivatedBluetoothScanningBtn.setEnabled(true);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                String msg = "________Bluetooth Device Stop Scanning Bluetooth 2.0________";
                MY_LOGGER.info(msg);
                MainActivity.activatedBluetoothScanningBtn.setEnabled(true);
                MainActivity.unactivatedBluetoothScanningBtn.setEnabled(false);
            }
        }
    }

}