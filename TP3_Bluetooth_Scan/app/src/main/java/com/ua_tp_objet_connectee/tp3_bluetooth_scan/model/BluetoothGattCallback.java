package com.ua_tp_objet_connectee.tp3_bluetooth_scan.model;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;

import androidx.core.app.ActivityCompat;

import com.ua_tp_objet_connectee.tp3_bluetooth_scan.MainActivity;

import java.util.logging.Logger;

public class BluetoothGattCallback extends android.bluetooth.BluetoothGattCallback {

    private static final Logger MY_LOGGER = Logger.getLogger(BluetoothGattCallback.class.getName());
    private BluetoothGatt bluetoothGattConnected;
    private BluetoothAdapter bluetoothAdapter;
    private final MainActivity mainActivity;

    public BluetoothGattCallback(MainActivity mainActivity) {
        this.bluetoothAdapter = null;
        this.bluetoothGattConnected = null;
        this.mainActivity = mainActivity;
        this.initialize();
        String msg = "_________The GATT Server Connection Is Open_________";
        MY_LOGGER.info(msg);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int status, int newState) {
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            String msg = "_________The GATT Server Is Connected Successfully_________";
            MY_LOGGER.info(msg);

            if (ActivityCompat.checkSelfPermission(this.mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                MY_LOGGER.info("We Do Nothing");
            }

            bluetoothGatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            String msg = "_________The GATT Server is disconnected_________";
            MY_LOGGER.info(msg);
        }

    }

    @Override
    public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
        if (this.mainActivity != null) {
            if (status == BluetoothGatt.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(this.mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    MY_LOGGER.info("We Do Nothing");
                }

                StringBuilder infoGattService = new StringBuilder("Information : \n");
                for (BluetoothGattService gattService : bluetoothGatt.getServices()) {
                    infoGattService.append("New Service : ").append(gattService.getUuid().toString()).append(" = ").append(Sample_gatt_attributes.lookup(gattService.getUuid().toString(), bluetoothGatt.getDevice().getName()));

                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                        infoGattService.append("\tNew Characteristics : ").append(gattCharacteristic.getUuid().toString()).append(" = ").append(Sample_gatt_attributes.lookup(gattService.getUuid().toString(), "default"));
                    }
                }

                Message message = this.mainActivity.getHandler().obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("disconnect_btn", "true");
                bundle.putString("deviceName", bluetoothGatt.getDevice().getName() + " (" + bluetoothGatt.getDevice().getAddress() + ")");
                bundle.putString("infoGattService", infoGattService.toString());
                message.setData(bundle);
                this.mainActivity.getHandler().sendMessage(message);
            }
        }
    }

    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            String msg = "_________Unable to obtain a BluetoothAdapter_________";
            MY_LOGGER.info(msg);
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
            if (ActivityCompat.checkSelfPermission(this.mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                MY_LOGGER.info("We Do Nothing");
            }

            bluetoothGattConnected = null;          //  When a new device will be connected
            bluetoothGattConnected = device.connectGatt(this.mainActivity, false, this);
            return bluetoothGattConnected != null;   // return true
        } catch (IllegalArgumentException exception) {
            String msg = "_________Device not found with provided address_________";
            MY_LOGGER.info(msg);
            return false;
        }
    }

    public void disconnect() {
        if (this.mainActivity != null) {
            if (bluetoothAdapter == null || bluetoothGattConnected == null) {
                MY_LOGGER.info("__________BluetoothAdapter or BluetoothDevice is not initialized for being disconnect__________");
                return;
            }
            if (ActivityCompat.checkSelfPermission(this.mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
        }
    }

    public void close() {
        if (bluetoothGattConnected == null) {
            String msg = "_________Impossible to close The GATT Server Connection_________";
            MY_LOGGER.info(msg);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this.mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
        bluetoothGattConnected = null;
        String msg = "_________The GATT Server Connection Is Closed_________";
        MY_LOGGER.info(msg);
    }

}