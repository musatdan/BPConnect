package com.KoAlco.BPConnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.HciStatus;
import com.welie.blessed.ScanFailure;
import com.welie.blessed.ScanMode;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FindingActivity extends AppCompatActivity {

    private BluetoothDevice device_to_connect;

    private final static int BLUETOOTH_PERMISSION_CODE = 101;
    private Handler handler = new Handler();
    private boolean scanning;
    private static final long SCAN_PERIOD = 15000;
    private BluetoothAdapter bluetoothAdapter;
    //private BluetoothManager bluetoothManager;
    private ListView btDevicesList;
    private ArrayList<BT_Device> bt_adapter_devices = new ArrayList<BT_Device>();
    private ArrayList<BluetoothDevice> devices_found = new ArrayList<BluetoothDevice>();

    private BluetoothCentralManager btManager;


    DeviceSearchAdapterList bt_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding);

    bluetoothAdapter = bluetoothAdapter.getDefaultAdapter();
    ((BluetoothApp) getApplication()).setBt_callback(btCentralManagerCallback);

    btManager = new BluetoothCentralManager(getApplicationContext(), btCentralManagerCallback, new Handler(Looper.getMainLooper()));

    ((BluetoothApp) getApplication()).setBt_manager(btManager);


    btDevicesList = (ListView) findViewById(R.id.devices_list);
    btDevicesList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    bt_adapter = new DeviceSearchAdapterList(this, R.layout.bt_list_items, bt_adapter_devices);

        btDevicesList.setAdapter(bt_adapter);

    AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            // получаем выбранный пункт
            BT_Device selectedState = (BT_Device) parent.getItemAtPosition(position);

            RadioButton btn = v.findViewById(R.id.bt_item_radioButton);

            bt_adapter.UncheckAll(bt_adapter_devices);

            selectedState.setChecked();
            btn.setChecked(true);

            btDevicesList.invalidateViews();

        }
    };

        btDevicesList.setOnItemClickListener(itemListener);

}

    private final BluetoothCentralManagerCallback btCentralManagerCallback = new BluetoothCentralManagerCallback() {
        @Override
        public void onDisconnectedPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull HciStatus status) {
            super.onDisconnectedPeripheral(peripheral, status);
            EventBus.getDefault().post(new MessageEvent("BLE Disconnected", 0));
        }

        @Override
        public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult result) {


            BluetoothDevice device = result.getDevice();

            String level = "Уровень сигнала: ";


            BT_Device bt_device = new BT_Device(device.getName(), device.getAddress(), level+result.getRssi());

            if(!findBTDevice(device.getAddress())) {
                devices_found.add(device);
                bt_adapter_devices.add(bt_device);
                btDevicesList.invalidateViews();
            }
        }

        @Override
        public void onScanFailed(@NotNull ScanFailure scanFailure) {
            super.onScanFailed(scanFailure);
            Toast.makeText(FindingActivity.this, "Ошибка сканирования", Toast.LENGTH_SHORT).show();
            scanning = false;
            btManager.stopScan();

            Button btn = FindingActivity.this.findViewById(R.id.button_search);
            btn.setText("Поиск устройств");
        }

    };

    private boolean findBTDevice (String address)
    {
        for (BT_Device device : bt_adapter_devices) {
            if(device.getAddress().equals(address))
                return true;
        }

        return false;
    }

    private void scanLeDevice() {
        if (!scanning) {

            bt_adapter_devices.clear();
            devices_found.clear();

            btDevicesList.invalidateViews();
            Button btn = this.findViewById(R.id.button_search);

            if(btn != null)
                btn.setText("Поиск...");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    btManager.stopScan();
                    Button btn = FindingActivity.this.findViewById(R.id.button_search);

                    if(btn != null)
                        btn.setText("Поиск устройств");
                }
            }, SCAN_PERIOD);

            Toast.makeText(this, "Сканирование...", Toast.LENGTH_SHORT).show();
            scanning = true;

            btManager.scanForPeripherals();
        } else {
            Toast.makeText(this, "Сканирование остановлено", Toast.LENGTH_SHORT).show();
            scanning = false;
            btManager.stopScan();;

            Button btn = FindingActivity.this.findViewById(R.id.button_search);
            if(btn != null)
                btn.setText("Поиск устройств");
        }
    }

    public void Connect_OnClick(View v) {
        BT_Device device_address = bt_adapter.FindChecked(bt_adapter_devices);

        scanning = false;
        btManager.stopScan();

        Button btn = FindingActivity.this.findViewById(R.id.button_search);
        btn.setText("Поиск устройств");

        if(device_address == null) {
            Toast.makeText(FindingActivity.this, "Необходимо выбрать устройство для подключения", Toast.LENGTH_SHORT).show();
            return;
        }
        for (BluetoothDevice device : devices_found) {
            if(device.getAddress().equals(device_address.getAddress()))
                device_to_connect = device;
        }

        ((BluetoothApp) getApplication()).setBt_device(device_to_connect);

        Intent intent = new Intent(FindingActivity.this, PressureActivity.class);
        startActivity(intent);
    }

    public void Find_Bt_Devices_OnClick(View v) {
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth выключен", Toast.LENGTH_LONG).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH)) {
                Toast.makeText(this, "Разрешения Bluetooth должны быть выданы", Toast.LENGTH_LONG).show();
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH},
                        BLUETOOTH_PERMISSION_CODE);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADMIN)) {
                Toast.makeText(this, "Разрешения Bluetooth должны быть выданы", Toast.LENGTH_LONG).show();
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                        BLUETOOTH_PERMISSION_CODE);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Разрешения поиска местоположения должны быть выданы", Toast.LENGTH_LONG).show();
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        BLUETOOTH_PERMISSION_CODE);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "Разрешения поиска местоположения должны быть выданы", Toast.LENGTH_LONG).show();
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        BLUETOOTH_PERMISSION_CODE);
            return;
        }

        scanLeDevice();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Разрешения выданы" , Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Разрешения не выданы, выдайте разрешения в настройках" , Toast.LENGTH_LONG).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}