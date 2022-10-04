package com.KoAlco.BPConnect;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;


public class BluetoothApp extends Application {
    private BluetoothDevice bt_device;
    private BluetoothCentralManager bt_manager;
    private BluetoothCentralManagerCallback bt_callback;

    public void setBt_callback(BluetoothCentralManagerCallback bt_callback) {
        this.bt_callback = bt_callback;
    }

    public BluetoothCentralManagerCallback getBt_callback() {
        return bt_callback;
    }

    public void setBt_manager(BluetoothCentralManager bt_manager) {
        this.bt_manager = bt_manager;
    }

    public BluetoothCentralManager getBt_manager() {
        return bt_manager;
    }

    public void setBt_device(BluetoothDevice bt_device) {
        this.bt_device = bt_device;
    }

    public BluetoothDevice getBt_device() {
        return bt_device;
    }

}
