package com.KoAlco.BPConnect;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;
import com.welie.blessed.PhyType;
import com.welie.blessed.WriteType;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PressureActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce;
    private BluetoothDevice device;
    private BluetoothPeripheral btDevice;
    private BluetoothCentralManagerCallback btCallback;
    private boolean discovered;
    private boolean searching = false;
    private BluetoothCentralManager btManager;

    private final UUID UUID_Service = UUID.fromString("e7810004-73ae-499d-8c15-faa9aef0c3f2");
    private final UUID UUID_Characteristic = UUID.fromString("e7810004-73ae-499d-8c15faa9aef0c3f2");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        discovered = false;
        EventBus.getDefault().register(this);

        btManager = ((BluetoothApp) getApplication()).getBt_manager();
        device = ((BluetoothApp) getApplication()).getBt_device();
        btCallback = ((BluetoothApp) getApplication()).getBt_callback();

        btDevice = btManager.getPeripheral(device.getAddress());
        //TODO return it \/
        btManager.connectPeripheral(btDevice, peripheralCallback);

        Handler timer = new Handler();
        timer.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!discovered)
                {
                    EventBus.getDefault().unregister(this);
                    btManager.cancelConnection(btDevice);

                    PressureActivity.this.finish();
                    return;
                }
            }
        }, 10000);

        Button btn_start = findViewById(R.id.button_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Start();
            }
        });

        Button btnStop = findViewById(R.id.button_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Stop();
            }
        });



        setContentView(R.layout.activity_pressure);
    }

    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
            super.onServicesDiscovered(peripheral);
            peripheral.setNotify(UUID_Service, UUID_Characteristic, true);

            if(discovered == false) {
                Toast.makeText(PressureActivity.this, "Подключено успешно", Toast.LENGTH_SHORT).show();
            }

            discovered = true;
        }

        @Override
        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            super.onNotificationStateUpdate(peripheral, characteristic, status);
        }

        @Override
        public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            super.onCharacteristicUpdate(peripheral, value, characteristic, status);

            if(discovered == false) {
                Toast.makeText(PressureActivity.this, "Подключено успешно", Toast.LENGTH_SHORT).show();
            }

            if(characteristic.equals(UUID_Characteristic))
            {
                TextView valueSPO = findViewById(R.id.spo2value);

                int valuespo2 = Byte.toUnsignedInt(value[0]);

                valueSPO.setText(String.valueOf(valuespo2) + " %");

            }

            discovered = true;
        }

        @Override
        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            super.onCharacteristicWrite(peripheral, value, characteristic, status);
        }

        @Override
        public void onDescriptorRead(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattDescriptor descriptor, @NotNull GattStatus status) {
            super.onDescriptorRead(peripheral, value, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattDescriptor descriptor, @NotNull GattStatus status) {
            super.onDescriptorWrite(peripheral, value, descriptor, status);
        }

        @Override
        public void onBondingStarted(@NotNull BluetoothPeripheral peripheral) {
            super.onBondingStarted(peripheral);
        }

        @Override
        public void onBondingSucceeded(@NotNull BluetoothPeripheral peripheral) {
            super.onBondingSucceeded(peripheral);
        }

        @Override
        public void onBondingFailed(@NotNull BluetoothPeripheral peripheral) {
            super.onBondingFailed(peripheral);
        }

        @Override
        public void onBondLost(@NotNull BluetoothPeripheral peripheral) {
            super.onBondLost(peripheral);
        }

        @Override
        public void onReadRemoteRssi(@NotNull BluetoothPeripheral peripheral, int rssi, @NotNull GattStatus status) {
            super.onReadRemoteRssi(peripheral, rssi, status);
        }

        @Override
        public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, @NotNull GattStatus status) {
            super.onMtuChanged(peripheral, mtu, status);
        }

        @Override
        public void onPhyUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull PhyType txPhy, @NotNull PhyType rxPhy, @NotNull GattStatus status) {
            super.onPhyUpdate(peripheral, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionUpdated(@NotNull BluetoothPeripheral peripheral, int interval, int latency, int timeout, @NotNull GattStatus status) {
            super.onConnectionUpdated(peripheral, interval, latency, timeout, status);

            if ((status == GattStatus.CONNECTION_CANCELLED) ||
                    (status == GattStatus.ERROR) ||
                    (status == GattStatus.INTERNAL_ERROR)) {

            }
        }

    };

    @Override
    public void onBackPressed() {
        
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            EventBus.getDefault().unregister(this);
            btManager.cancelConnection(btDevice);

            PressureActivity.this.finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Нажмите \"Назад\" ещё раз чтобы вернуться к поиску", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void Start ()
    {
        String str = "SPO2enable";
        if (discovered) {
            btDevice.writeCharacteristic(UUID_Service,
                    UUID.fromString("e7810001-73ae-499d-8c15-faa9aef0c3f2"),
                    str.getBytes(StandardCharsets.UTF_8),
                    WriteType.WITHOUT_RESPONSE);
        } else
            Toast.makeText(this, "Подождите подключения...", Toast.LENGTH_SHORT).show();

    }

    private void Stop ()
    {
        String str = "SPO2disable";
        if (discovered) {
            btDevice.writeCharacteristic(UUID_Service,
                    UUID.fromString("e7810001-73ae-499d-8c15-faa9aef0c3f2"),
                    str.getBytes(StandardCharsets.UTF_8),
                    WriteType.WITHOUT_RESPONSE);
        } else
            Toast.makeText(this, "Подождите подключения...", Toast.LENGTH_SHORT).show();

    }
}