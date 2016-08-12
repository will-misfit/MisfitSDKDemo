package com.misfit.misfitsdkdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.misfit.misfitsdk.MFAdapter;
import com.misfit.misfitsdk.callback.MFScanCallback;
import com.misfit.misfitsdk.device.MFDevice;
import com.misfit.misfitsdk.enums.MFDefine;
import com.misfit.misfitsdk.enums.MFDeviceType;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;


public class ScanListActivity extends AppCompatActivity implements MFScanCallback {

    private final static String TAG = "ScanListActivity";
    private final static String EXT_DEVICE_TYPE = "device_type";
    private final static String EXT_SERIAL_NUNBER = "serial_number";

    DeviceAdapter mAdapter;

    @BindView(R.id.list_device)
    ListView mListDevice;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_list);
        ButterKnife.bind(this);

        setResult(RESULT_CANCELED);

        int deviceTpe = getIntent().getIntExtra(EXT_DEVICE_TYPE, MFDeviceType.ALL);
        mAdapter = new DeviceAdapter(this);
        mListDevice.setAdapter(mAdapter);
        MFAdapter.getInstance().startScanning(deviceTpe, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MFAdapter.getInstance().stopScanning();
    }

    public static Intent getOpenIntent(Context context, int deviceType) {
        Intent intent = new Intent(context, ScanListActivity.class);
        intent.putExtra(EXT_DEVICE_TYPE, deviceType);
        return intent;
    }

    public static String getSerialNumberFromData(Intent intent) {
        if (intent == null) {
            return null;
        } else {
            return intent.getStringExtra(EXT_SERIAL_NUNBER);
        }
    }

    @Override
    public void onScanResult(final MFDevice device, final int rssi) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "found dev=" + device.getSerialNumber());
                mAdapter.updateDevice(device, rssi);
            }
        });
    }

    @Override
    public void onScanFailed(@MFDefine.ScanFailedReason final int reason) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "scan failed, reason : " + reason);
            }
        });
    }

    @OnItemClick(R.id.list_device)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceAdapter.DeviceItem item = mAdapter.mData.get(position);
        Intent intent = new Intent();
        intent.putExtra(EXT_SERIAL_NUNBER, item.device.getSerialNumber());
        setResult(RESULT_OK, intent);
        finish();
    }

    static class DeviceAdapter extends SimpleListAdapter<DeviceAdapter.DeviceItem, DeviceAdapter.ViewHolder> {

        public DeviceAdapter(Context context) {
            super(context, new ArrayList<DeviceItem>(), R.layout.row_device);
        }

        @Override
        protected ViewHolder createViewHolder(View itemView, int type) {
            return new ViewHolder(itemView);
        }

        @Override
        protected void bindData(ViewHolder holder, DeviceItem item, int position) {
            holder.name.setText(MFDeviceType.getDeviceTypeText(item.device.getSerialNumber()));
            holder.serial.setText(item.device.getSerialNumber());
            holder.rssi.setText("-" + item.rssi);
        }

        public void updateDevice(MFDevice device, int rssi) {
            for (DeviceItem item : mData) {
                if (item.device.getSerialNumber().equals(device.getSerialNumber())) {
                    item.device = device;
                    item.rssi = rssi;
                    Log.w(TAG, "update");
                    notifyDataSetChanged();
                    return;
                }
            }
            mData.add(new DeviceItem(device, rssi));
            Log.w(TAG, "add");
            notifyDataSetChanged();
        }

        static class ViewHolder extends SimpleListAdapter.ViewHolder {

            @BindView(R.id.text_name)
            TextView name;

            @BindView(R.id.text_serial_number)
            TextView serial;

            @BindView(R.id.text_rssi)
            TextView rssi;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        static class DeviceItem {
            MFDevice device;
            int rssi;

            public DeviceItem(MFDevice device, int rssi) {
                this.device = device;
                this.rssi = rssi;
            }
        }
    }
}
