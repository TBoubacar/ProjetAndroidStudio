package com.ua_tp_objet_connectee.tp3_bluetooth_scan.model;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.ua_tp_objet_connectee.tp3_bluetooth_scan.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class BluetoothLeService extends Service {

    private static Logger MY_LOGGER = Logger.getLogger(BluetoothLeService.class.getName());
    private MainActivity mainActivity;
    private BluetoothGattCallback bluetoothGattCallback;
    private final static int STATE_DISCONNECTED = 0;
    private final static int STATE_CONNECTED = 2;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Binder binder;

    public BluetoothLeService(MainActivity mainActivity) {
        this.bluetoothAdapter = null;
        this.bluetoothGatt = null;
        this.binder = new LocalBinder();
        this.mainActivity = mainActivity;
        this.bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothLeService.STATE_CONNECTED) {
                    String msg = "_________Successfully connected to the GATT Server_________";
                    MY_LOGGER.info(msg);

                    if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }

                    bluetoothGatt.discoverServices();
                } else if (newState == BluetoothLeService.STATE_DISCONNECTED) {
                    String msg = "_________Disconnected from the GATT Server_________";
                    MY_LOGGER.info(msg);
                }
            }
        };
        this.initialize();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            mainActivity.disconnect_btn.setEnabled(true);
            mainActivity.deviceConnectedName.setText(bluetoothGatt.getDevice().getName() + " (" + bluetoothGatt.getDevice().getAddress() + ")");
            displayGattServices();

            String msg = "_________Bluetooth Device (" + bluetoothGatt.getDevice().getName() + ") is connected successfuly_________";
            Toast.makeText(mainActivity, msg, Toast.LENGTH_SHORT).show();
        } else {
            String msg = "_________Bluetooth Device (" + bluetoothGatt.getDevice().getName() + ") is connected successfuly_________";
            Toast.makeText(mainActivity, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            String msg = "_________Unable to obtain a BluetoothAdapter_________";
            MY_LOGGER.info(msg);
            Toast.makeText(this.mainActivity, msg, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            String msg = "_________BluetoothAdapter not initialized or unspecified address_________";
            MY_LOGGER.info(msg);
            return false;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }

            bluetoothGatt = device.connectGatt(mainActivity, false, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            String msg = "_________Device not found with provided address_________";
            MY_LOGGER.info(msg);
            return false;
        }
    }

    public void disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            MY_LOGGER.info("__________BluetoothAdapter not initialized__________");
            return;
        }
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        mainActivity.disconnect_btn.setEnabled(false);
        mainActivity.deviceConnectedName.setText(null);
        mainActivity.deviceConnectedInfo.setText(null);
        bluetoothGatt.disconnect();
    }

    public void displayGattServices() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        String infoGattService = "";
        for (BluetoothGattService gattService : this.getSupportedGattServices()) {
            infoGattService += "New Service : " + Sample_gatt_attributes.lookup(gattService.getUuid().toString(), bluetoothGatt.getDevice().getName());

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();

                currentCharaData.put("LIST_NAME", Sample_gatt_attributes.lookup(gattService.getUuid().toString(), "unknownCharaString"));
                currentCharaData.put("LIST_UUID", gattService.getUuid().toString());
            }
        }

                String infoDevice = "Address of device : " + bluetoothGatt.getDevice().getAddress() + "\n" +
                "Name of device : " + bluetoothGatt.getDevice().getName() + "\n" +
                "Bluetooth Class : " + bluetoothGatt.getDevice().getBluetoothClass() + "\n" +
                "Type of device : " + bluetoothGatt.getDevice().getType() + "\n";
        mainActivity.deviceConnectedInfo.setText(infoDevice);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) {
            MY_LOGGER.info("__________bluetoothGatt not initialized__________");
            return null;
        }

        return bluetoothGatt.getServices();
    }

    public void close() {
        if (bluetoothGatt == null) {
            String msg = "_________Impossible to close The GATT Server Connexion_________";
            MY_LOGGER.info(msg);
            return;
        }
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
        String msg = "_________Close The GATT Server Connexion_________";
        MY_LOGGER.info(msg);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

}