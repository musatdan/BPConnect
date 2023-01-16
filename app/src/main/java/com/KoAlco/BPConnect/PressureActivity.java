package com.KoAlco.BPConnect;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;
import com.welie.blessed.PhyType;
import com.welie.blessed.WriteType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class PressureActivity extends AppCompatActivity {

    private CombinedChart c_chart;
    private CombinedData combinedData;
    private ArrayList<Entry> lineEntriesHB;
    private LineDataSet lineDataSetLB;
    private LineData lineData;

    private BluetoothApp bluetoothApp;
    private BluetoothDevice bluetoothDevice;

    private boolean doubleBackToExitPressedOnce;
    private BluetoothDevice device;
    private BluetoothPeripheral btDevice;
    private BluetoothCentralManagerCallback btCallback;
    private boolean discovered;
    private boolean searching = false;
    private BluetoothCentralManager btManager;

    private final UUID UUID_Service = UUID.fromString("5cb81400-84dd-46c4-aef8-b77378001d35");
    private final UUID UUID_Characteristic = UUID.fromString("5cb81401-84dd-46c4-aef8-b77378001d35");

    @Subscribe
    public void onMessageEvent (MessageEvent messageEvent)
    {
        switch (messageEvent.message)
        {
            case "BLE Disconnected":
            {
                EventBus.getDefault().unregister(this);
                btManager.cancelConnection(btDevice);

                PressureActivity.this.finish();
                break;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressure);

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

        lineEntriesHB = new ArrayList<>();

        for (int i = 0; i < 400; i++) {
            lineEntriesHB.add(new Entry(i, 0));
        }

        c_chart = findViewById(R.id.chart_pressure);
        combinedData = new CombinedData();

        lineDataSetLB = new LineDataSet(lineEntriesHB, "Давление в манжете");

        lineDataSetLB.setColor(Color.rgb(240, 50, 50));
        lineDataSetLB.setLineWidth(2.5f);
        lineDataSetLB.setCircleColor(Color.rgb(240, 50, 50));
        lineDataSetLB.setDrawCircles(false);

        lineData = new LineData(lineDataSetLB);

        combinedData.setData(lineData);

        YAxis yAxis = c_chart.getAxisLeft();

        YAxis yAxisRight = c_chart.getAxisRight();
        yAxisRight.setEnabled(false);
        c_chart.setAutoScaleMinMaxEnabled(true);
        c_chart.getDescription().setEnabled(false);
        c_chart.getLegend().setTextSize(16);
        c_chart.setData(combinedData);
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


                String str = new String(value, StandardCharsets.UTF_8);

                if(str.contains("BLPRE"))
                {
                    Scanner scanner = new Scanner(str).useDelimiter("BLPRE");

                    if(str.contains("ERR")) {
                        TextView SYS_text = findViewById(R.id.text_sys);
                        SYS_text.setText("ERR");

                        TextView DIA_text = findViewById(R.id.text_dia);
                        DIA_text.setText("ERR");

                        TextView PULSE_text = findViewById(R.id.text_pulse_1);
                        PULSE_text.setText("ERR");

                        Button btn_start = findViewById(R.id.button_start);

                        btn_start.setEnabled(true);
                        btn_start.setText("повторить измерение");
                    }
                    else {
                        String str1 = new String(scanner.next());

                        Scanner scanner1 = new Scanner(str1).useDelimiter(":");

                        TextView SYS_text = findViewById(R.id.text_sys);
                        SYS_text.setText(String.valueOf(scanner1.nextInt()));

                        TextView DIA_text = findViewById(R.id.text_dia);
                        DIA_text.setText(String.valueOf(scanner1.nextInt()));

                        TextView PULSE_text = findViewById(R.id.text_pulse_1);
                        PULSE_text.setText(String.valueOf(scanner1.nextInt()));

                        Button btn_start = findViewById(R.id.button_start);

                        btn_start.setEnabled(true);
                        btn_start.setText("начать измерение");
                    }

                }

                if(str.contains("BLPRP")){
                    Scanner scanner = new Scanner(str).useDelimiter("BLPRP");

                    int a = (int) scanner.nextInt();

                        lineEntriesHB.remove(0);

                        for (Entry entry : lineEntriesHB) {
                            entry.setX(entry.getX() - 2);
                        }

                        lineEntriesHB.add(new Entry(399, a));

                        c_chart.invalidate();

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
        String str = "BLPRF";
        if (discovered) {
            btDevice.writeCharacteristic(UUID_Service,
                    UUID_Characteristic,
                    str.getBytes(StandardCharsets.UTF_8),
                    WriteType.WITH_RESPONSE);

            TextView SYS_text = findViewById(R.id.text_sys);
            SYS_text.setText(String.valueOf(0));

            TextView DIA_text = findViewById(R.id.text_dia);
            DIA_text.setText(String.valueOf(0));

            TextView PULSE_text = findViewById(R.id.text_pulse_1);
            PULSE_text.setText(String.valueOf(0));

            lineEntriesHB.clear();

            for (int i = 0; i < 400; i++) {
                lineEntriesHB.add(new Entry(i, 0));
            }

            Button btn_start = findViewById(R.id.button_start);

            btn_start.setEnabled(false);
            btn_start.setText("измерение...");

            c_chart.invalidate();

        } else
            Toast.makeText(this, "Подождите подключения...", Toast.LENGTH_SHORT).show();

    }
}