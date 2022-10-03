package com.KoAlco.BPConnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

public class DeviceSearchAdapterList extends ArrayAdapter<BT_Device> {

        private LayoutInflater inflater;
        private int layout;
        private List<BT_Device> bt_devices;

        public DeviceSearchAdapterList(Context context, int resource, List<BT_Device> bt_devices) {
            super(context, resource, bt_devices);
            this.bt_devices = bt_devices;
            this.layout = resource;
            this.inflater = LayoutInflater.from(context);
        }

        public void RefreshList(List<BT_Device> bt_devices) {
            this.bt_devices = bt_devices;
        }

        public BT_Device FindChecked(List<BT_Device> bt_devices) {
            for (BT_Device device : bt_devices) {
                if (device.isChecked())
                    return device;
            }

            return null;
        }


        public void UncheckAll(List<BT_Device> bt_devices) {
            for (BT_Device device : bt_devices) {
                device.setUnchecked();
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(this.layout, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            BT_Device bt_device = bt_devices.get(position);

            viewHolder.nameView.setText(bt_device.getName());
            viewHolder.addressView.setText(bt_device.getAddress());
            viewHolder.level.setText(bt_device.getLevel());
            viewHolder.button.setChecked(bt_device.isChecked());


            return convertView;
        }

        private class ViewHolder {
            final TextView nameView, addressView, level;
            final RadioButton button;

            ViewHolder(View view) {
                nameView = view.findViewById(R.id.bt_name);
                addressView = view.findViewById(R.id.bt_address);
                button = view.findViewById(R.id.bt_item_radioButton);
                level = view.findViewById(R.id.level);
            }
        }
    }

