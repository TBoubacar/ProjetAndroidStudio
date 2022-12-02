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
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.ua_tp_objet_connectee.tp3_bluetooth_scan.MainActivity;

import java.util.List;
import java.util.logging.Logger;

public class BluetoothLeService extends Service {

    private static Logger MY_LOGGER = Logger.getLogger(BluetoothLeService.class.getName());
    private MainActivity mainActivity;
    private BluetoothGattCallback bluetoothGattCallback;
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
                if (newState == BluetoothGatt.STATE_CONNECTED) {
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
                    this.onServicesDiscovered(bluetoothGatt, BluetoothGatt.STATE_CONNECTED);
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    String msg = "_________GATT Server is disconnected_________";
                    MY_LOGGER.info(msg);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
                if (mainActivity != null) {
                    if (status == BluetoothGatt.STATE_CONNECTED) {
                        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                        };
                        displayGattServices();

                        String msg = "\t_________Bluetooth Device (" + bluetoothGatt.getDevice().getName() + ") is connected successfuly_________";
                        MY_LOGGER.info(msg);
                    }
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
        String infoGattService = "Informations : \n";
        for (BluetoothGattService gattService : this.getSupportedGattServices()) {
            infoGattService += "New Service : " + gattService.getUuid().toString() + " = " + Sample_gatt_attributes.lookup(gattService.getUuid().toString(), bluetoothGatt.getDevice().getName());

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                infoGattService += "\tNew Characteristics : " + gattCharacteristic.getUuid().toString() + " = " + Sample_gatt_attributes.lookup(gattService.getUuid().toString(), "default");
            }
        }
        MY_LOGGER.info(infoGattService);

        Message message = mainActivity.handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("disconnect_btn", "true");
        bundle.putString("deviceName", bluetoothGatt.getDevice().getName() + " (" + bluetoothGatt.getDevice().getAddress() + ")");
        bundle.putString("infoGattService", infoGattService);
        message.setData(bundle);
        mainActivity.handler.sendMessage(message);
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
        if (mainActivity != null) {
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
            mainActivity.deviceConnectedName.setText("");
            mainActivity.deviceConnectedInfo.setText("");
            bluetoothGatt.disconnect();
        }
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