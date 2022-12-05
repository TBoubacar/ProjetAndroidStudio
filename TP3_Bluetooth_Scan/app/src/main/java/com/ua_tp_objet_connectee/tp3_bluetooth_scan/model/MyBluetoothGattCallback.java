package com.ua_tp_objet_connectee.tp3_bluetooth_scan.model;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;

import androidx.core.app.ActivityCompat;

import com.ua_tp_objet_connectee.tp3_bluetooth_scan.MainActivity;

import java.util.logging.Logger;

public class MyBluetoothGattCallback extends android.bluetooth.BluetoothGattCallback {

    private static final Logger MY_LOGGER = Logger.getLogger(MyBluetoothGattCallback.class.getName());
    private final MainActivity mainActivity;

    public MyBluetoothGattCallback(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        String msg = "_________The GATT Server Connection Is Open_________";
        MY_LOGGER.info(msg);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int status, int newState) {
        super.onConnectionStateChange(bluetoothGatt, status, newState);
        if (newState == BluetoothGatt.STATE_CONNECTED) {
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
            String msg = "_________The GATT Server Is Connected Successfully to <" + bluetoothGatt.getDevice().getName() + "(" + bluetoothGatt.getDevice().getAddress() + ")" + ">_________";
            MY_LOGGER.info(msg);
            bluetoothGatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            String msg = "_________The GATT Server is disconnected from <" + bluetoothGatt.getDevice().getName() + "(" + bluetoothGatt.getDevice().getAddress() + ")" + ">__________";
            MY_LOGGER.info(msg);
        }

    }

    @Override
    public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
        super.onServicesDiscovered(bluetoothGatt, status);
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

                StringBuilder infoGattService = new StringBuilder();
                for (BluetoothGattService gattService : bluetoothGatt.getServices()) {
                    infoGattService.append("New Service : ").append(gattService.getUuid().toString()).append(" = ").append(Sample_gatt_attributes.lookup(gattService.getUuid().toString(), bluetoothGatt.getDevice().getName())).append("\n");

                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                        infoGattService.append("\tNew Characteristics : ").append(gattCharacteristic.getUuid().toString()).append(" = ").append(Sample_gatt_attributes.lookup(gattService.getUuid().toString(), "default")).append("\n");
                    }
                }

                Message message = this.mainActivity.getHandler().obtainMessage();
                Bundle bundle = new Bundle();

                String deviceName = bluetoothGatt.getDevice().getName() + " (" + bluetoothGatt.getDevice().getAddress() + ")";

                bundle.putString("deviceName", deviceName);
                bundle.putString("infoGattService", infoGattService.toString());
                message.setData(bundle);

                this.mainActivity.getHandler().sendMessage(message);
            }
        }
    }

}