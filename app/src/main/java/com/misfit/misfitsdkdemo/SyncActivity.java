package com.misfit.misfitsdkdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.misfit.misfitsdk.MFAdapter;
import com.misfit.misfitsdk.Version;
import com.misfit.misfitsdk.callback.MFDataCallback;
import com.misfit.misfitsdk.callback.MFGestureCallback;
import com.misfit.misfitsdk.callback.MFGetCallback;
import com.misfit.misfitsdk.callback.MFHIdConnectionCallback;
import com.misfit.misfitsdk.callback.MFOperationResultCallback;
import com.misfit.misfitsdk.callback.MFOtaCallback;
import com.misfit.misfitsdk.callback.MFScanCallback;
import com.misfit.misfitsdk.device.MFDevice;
import com.misfit.misfitsdk.enums.MFDefine;
import com.misfit.misfitsdk.enums.MFDeviceType;
import com.misfit.misfitsdk.enums.MFEvent;
import com.misfit.misfitsdk.enums.MFGesture;
import com.misfit.misfitsdk.enums.MFMappingType;
import com.misfit.misfitsdk.model.MFActivitySessionGroup;
import com.misfit.misfitsdk.model.MFDeviceInfo;
import com.misfit.misfitsdk.model.MFGraphItem;
import com.misfit.misfitsdk.model.MFSleepSession;
import com.misfit.misfitsdk.model.MFSyncParams;
import com.misfit.misfitsdk.model.SupportedFeature;
import com.misfit.misfitsdk.utils.MLog;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SyncActivity extends AppCompatActivity {

    private final static String TAG = "SyncActivity";

    private final static int REQ_SCAN = 1;

    @BindView(R.id.btn_stop_listen_gesture)
    Button mBtnStopListenGesture;
    @BindView(R.id.tv_log)
    TextView mLogTv;
    @BindView(R.id.text_serial_number)
    TextView mTextSerialNumber;
    @BindView(R.id.text_device_name)
    TextView mTextDeviceName;
    @BindView(R.id.switch_activate)
    Switch mSwitchActivate;
    @BindView(R.id.switch_tagging_response)
    Switch mSwitchTaggingResponse;
    @BindViews({R.id.btn_sync,
            R.id.btn_get_config,
            R.id.btn_play_call,
            R.id.btn_play_text,
            R.id.btn_stop_animation,
            R.id.btn_map_button,
            R.id.btn_start_scan,
            R.id.btn_stop_scan,
            R.id.btn_hid_connect,
            R.id.btn_hid_disconnect,
            R.id.btn_play_animation,
            R.id.btn_unmap_all,
            R.id.btn_start_listen_gesture,
            R.id.btn_stop_listen_gesture,
            R.id.btn_get_mapping,
            R.id.btn_map_activity_tagging,
            R.id.btn_write_setting})
    List<View> syncPanel;

    private int[] mDeviceTypeInts = new int[]{
            MFDeviceType.ALL,
            MFDeviceType.SHINE,
            MFDeviceType.FLASH,
            MFDeviceType.SWAROVSKI_SHINE,
            MFDeviceType.SPEEDO_SHINE,
            MFDeviceType.SHINE_MK_II,
            MFDeviceType.SHINE2,
            MFDeviceType.FLASH_LINK,
            MFDeviceType.SILVRETTA,
            MFDeviceType.RAY
    };

    private MFEvent[] mEvents = new MFEvent[]{
            MFEvent.MEDIA_NEXT_SONG,
            MFEvent.MEDIA_PLAY_PAUSE,
            MFEvent.MEDIA_PREVIOUS_SONG,
            MFEvent.MEDIA_VOLUME_DOWN,
            MFEvent.MEDIA_VOLUME_UP_OR_SELFIE,
    };

    private MFGesture[] mShine2Gesture = new MFGesture[]{
            MFGesture.SHINE2_TRIPLE_TAP
    };

    private MFGesture[] mRayGesture = new MFGesture[]{
            MFGesture.RAY_TRIPLE_TAP
    };
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Gson mGson;
    private MFDevice mDevice;

    private MFHIdConnectionCallback mHidCallback = new MFHIdConnectionCallback() {
        @Override
        public void onConnectionStateChange(String serialNumber, int state) {
            log(serialNumber + " connection state changed, new state=" + state);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        ButterKnife.bind(this);

        mLogTv.setVerticalScrollBarEnabled(true);
        mLogTv.setHorizontallyScrolling(true);
        mLogTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        mGson = new GsonBuilder().setPrettyPrinting().create();
        setSyncPanelEnabled(false);

        MFAdapter sdkAdapter = MFAdapter.getInstance();
        sdkAdapter.init(this.getApplicationContext(),
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                "5c203ef8-d62a-11e5-ab30-625662870761");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            String versionInfo = String.format("MisfitSDK-%s, Demo-%s",
                    Version.getVersionName(),
                    BuildConfig.VERSION_NAME);
            Toast.makeText(this, versionInfo, Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_start_scan)
    void startScan() {
        String[] deviceTypeStrings = new String[mDeviceTypeInts.length];
        for (int i = 0; i < mDeviceTypeInts.length; i++) {
            deviceTypeStrings[i] = MFDeviceType.getDeviceTypeText(mDeviceTypeInts[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item,
                deviceTypeStrings);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose device type")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedDeviceType = mDeviceTypeInts[which];
                        MFAdapter.getInstance().startScanning(selectedDeviceType, new MFScanCallback() {
                            @Override
                            public void onScanResult(MFDevice device, int rssi) {
                                Log.i("outside", "found device=" + device.getSerialNumber());
                            }

                            @Override
                            public void onScanFailed(@MFDefine.ScanFailedReason int reason) {
                                Log.i("outside", "failed=" + reason);
                            }
                        });
                    }
                }).show();
    }

    @OnClick(R.id.btn_stop_scan)
    void stopScan() {
        MFAdapter.getInstance().stopScanning();
    }

    @OnClick(R.id.btn_scan)
    void scan() {
        String[] deviceTypeStrings = new String[mDeviceTypeInts.length];
        for (int i = 0; i < mDeviceTypeInts.length; i++) {
            deviceTypeStrings[i] = MFDeviceType.getDeviceTypeText(mDeviceTypeInts[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item,
                deviceTypeStrings);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose device type")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedDeviceType = mDeviceTypeInts[which];
                        Intent intent = ScanListActivity.getOpenIntent(SyncActivity.this, selectedDeviceType);
                        log(String.format("start scan, device type is %s ", MFDeviceType.getDeviceTypeText(selectedDeviceType)));
                        startActivityForResult(intent, REQ_SCAN);
                    }
                }).show();
    }

    @OnClick(R.id.btn_write_setting)
    void writeSettings() {
        String serialNumber = "";
        if (mDevice != null) {
            serialNumber = mDevice.getSerialNumber();
        }
        startActivity(SettingsActivity.getOpenIntent(this, serialNumber));
    }

    @OnClick(R.id.btn_by_serial)
    void getBySerialNumber() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null, false);
        final EditText editView = (EditText) view.findViewById(R.id.edit);
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String serial = editView.getText().toString().toUpperCase(Locale.US);
                        if (TextUtils.isEmpty(serial)) {
                            Toast.makeText(SyncActivity.this, "Serial number can not be empty", Toast.LENGTH_SHORT).show();
                        } else {
                            updateDeviceInfo(serial);
                            setSyncPanelEnabled(true);
                        }
                    }
                })
                .show();
    }

    @OnClick(R.id.btn_get_config)
    void getConfig() {
        if (mDevice != null) {
            setSyncPanelEnabled(false);
            mDevice.getDeviceInfo(new MFGetCallback<MFDeviceInfo>() {
                @Override
                public void onGet(final MFDeviceInfo data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log(mGson.toJson(data));
                        }
                    });
                }
            }, new OperationCallback("getConfig"));
        }
    }

    @OnClick(R.id.btn_set_goal)
    void setGoal() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null, false);
        final EditText editView = (EditText) view.findViewById(R.id.edit);
        editView.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String goal = editView.getText().toString().toUpperCase(Locale.US);
                        if (TextUtils.isEmpty(goal)) {
                            Toast.makeText(SyncActivity.this, "Can not be empty", Toast.LENGTH_SHORT).show();
                        } else {
                            mDevice.setGoal(Integer.valueOf(goal), new OperationCallback("setGoal"));
                        }
                    }
                })
                .show();
    }


    @OnClick(R.id.btn_stop_animation)
    void stopAnimation() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.STOP_ANIMATION)) {
            mDevice.stopAnimation(new OperationCallback("stopAnimation"));
        }
    }

    @OnClick(R.id.btn_play_call)
    void playCall() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.CALL_TEXT_NOTIFICATION)) {
            mDevice.playCallNotification(new OperationCallback("playCallNotification"));
        }
    }

    @OnClick(R.id.btn_play_text)
    void playText() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.CALL_TEXT_NOTIFICATION)) {
            mDevice.playTextNotification(new OperationCallback("playTextNotification"));
        }
    }

    @OnClick(R.id.btn_hid_connect)
    void hidConnect() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.HID)) {
            mDevice.connectHid(mHidCallback, new OperationCallback("hidConnect"));
        }
    }

    @OnClick(R.id.btn_hid_disconnect)
    void hidDisConnect() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.HID)) {
            mDevice.disconnectHid(null);
        }
    }

    @OnClick(R.id.btn_map_button)
    void mapButton() {
        if (mDevice == null || !mDevice.hasFeature(SupportedFeature.MAP_BUTTON)) {
            Toast.makeText(this, "Not supported yet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mDevice.getDeviceType() == MFDeviceType.SHINE2) {
            mapButton(mShine2Gesture);
        } else if (mDevice.getDeviceType() == MFDeviceType.RAY) {
            mapButton(mRayGesture);
        } else {
            Toast.makeText(this, "Not supported yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void mapButton(final MFGesture[] gestures) {
        String[] strings = new String[gestures.length];
        for (int i = 0; i < gestures.length; i++) {
            strings[i] = gestures[i].name();
        }
        DialogUtils.showSlectionDialog(this, strings, "Select Gesture", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final MFGesture selected = gestures[which];
                String[] strings = new String[mEvents.length];
                for (int i = 0; i < mEvents.length; i++) {
                    strings[i] = mEvents[i].name();
                }
                DialogUtils.showSlectionDialog(SyncActivity.this, strings, "Select Event", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setSyncPanelEnabled(false);
                        mDevice.mapButton(selected, mEvents[which], new OperationCallback("mapButton"));
                    }
                });
            }
        });
    }

    @OnClick(R.id.btn_map_activity_tagging)
    void mapActivityTagging() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.MAP_ACTIVITY_TAGGING)) {
            setSyncPanelEnabled(false);
            mDevice.mapActivityTagging(new OperationCallback("mapActivityTagging"));
        }
    }

    @OnClick(R.id.btn_get_mapping)
    void getMappingType() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.GET_MAPPING_TYPE)) {
            setSyncPanelEnabled(false);
            mDevice.getMappingType(new MFGetCallback<MFMappingType>() {
                @Override
                public void onGet(final MFMappingType data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log("Mapping type:" + data);
                        }
                    });
                }
            }, new OperationCallback("getMappingType"));
        }
    }

    @OnClick(R.id.btn_unmap_all)
    void unmapAll() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.UNMAP_ALL)) {
            setSyncPanelEnabled(false);
            mDevice.unmapAll(new OperationCallback("unmapAll"));
        }
    }

    @OnClick(R.id.btn_sync)
    void sync() {
        MFSyncParams syncParams = createSyncParams();
        mDevice.startSync(syncParams,
                new OperationCallback("sync"),
                new MFGetCallback<MFDeviceInfo>() {
                    @Override
                    public void onGet(final MFDeviceInfo data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                log(mGson.toJson(data));
                            }
                        });
                    }
                }, new MFDataCallback() {
                    @Override
                    public void onActivitySessionGroupSynced(MFActivitySessionGroup activitySessionGroup) {
                        log(Printer.getActivitySessionText(activitySessionGroup.getActivitySessions()));
                        log(Printer.getGapSessionText(activitySessionGroup.getGapSessions()));
                    }

                    @Override
                    public void onSleepSessionSynced(List<MFSleepSession> MFSleepSessions) {
                        log(Printer.getSleepSessionText(MFSleepSessions));
                    }

                    @Override
                    public void onGraphItemSynced(List<MFGraphItem> MFGraphItems) {
                        log(Printer.getGraphItemText(MFGraphItems));
                    }
                }, new MFOtaCallback() {
                    @Override
                    public void onEnter() {
                        log("ota enter");
                    }

                    @Override
                    public void onCompleted() {
                        log("ota completed");
                    }

                    @Override
                    public void onProgress(float v) {
                        log("ota progress=" + v);
                    }
                });
        setSyncPanelEnabled(false);
    }

    @OnClick(R.id.btn_play_animation)
    void playAnimation() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.PLAY_ANIMATION)) {
            mDevice.playAnimation(new OperationCallback("playAnimation"));
        }
    }

    @OnClick(R.id.btn_stop)
    void stop() {
        mDevice.stopOperation();
        setSyncPanelEnabled(true);
    }

    @OnClick(R.id.btn_start_listen_gesture)
    void startListenGesture() {
        if (mDevice != null && mDevice.hasFeature(SupportedFeature.LISTEN_GESTURE)) {
            mDevice.startListenGesture(new MFGestureCallback() {
                @Override
                public void onReady() {
                    log("gesture is ready for receiving. ");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSyncPanelEnabled(false);
                            mBtnStopListenGesture.setEnabled(true);
                        }
                    });
                }

                @Override
                public void onGestureReceived(MFGesture gesture) {
                    log("gesture is " + gesture.name());
                }

                @Override
                public void onStopped() {
                    log("gesture is stopped. ");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSyncPanelEnabled(true);
                        }
                    });
                }

                @Override
                public void onHeartbeatReceived() {
                    log("gesture's heart beat is received. ");
                }
            }, new OperationCallback("startListenGesture"));
        }
    }

    @OnClick(R.id.btn_stop_listen_gesture)
    void stopListenGesture() {
        if (mDevice != null) {
            mDevice.stopListenGesture(new OperationCallback("stopListenGesture"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            log(String.format(Locale.US, "result not ok, req = %d, result = %d", requestCode, resultCode));
            return;
        }
        switch (requestCode) {
            case REQ_SCAN:
                updateDeviceInfo(ScanListActivity.getSerialNumberFromData(data));
                setSyncPanelEnabled(true);
                break;
        }
    }

    private void updateDeviceInfo(String serialNumber) {
        log("updated device, serialNumber=" + serialNumber);
        mDevice = MFAdapter.getInstance().getDevice(serialNumber);
        mTextSerialNumber.setText(serialNumber);
        mTextDeviceName.setText(MFDeviceType.getDeviceTypeText(serialNumber));
        mSwitchTaggingResponse.setVisibility(MFDeviceType.getDeviceType(serialNumber) == MFDeviceType.FLASH ? View.VISIBLE : View.GONE);
    }

    private void setSyncPanelEnabled(boolean enabled) {
        for (View view : syncPanel) {
            view.setEnabled(enabled);
        }
    }

    private MFSyncParams createSyncParams() {
        MFSyncParams MFSyncParams = new MFSyncParams(DataSource.getDefaultMale(),
                mSwitchActivate.isChecked());
        MFSyncParams.lastGraphItem = DataSource.getFakeGraphItem();

        return MFSyncParams;
    }

    private void log(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, msg);
                mLogTv.append(msg + "\n");
            }
        });
    }

    private class OperationCallback implements MFOperationResultCallback {
        private String operationName;

        public OperationCallback(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public void onSucceed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String msg = operationName + " finished";
                    log(msg);
                    setSyncPanelEnabled(true);
                }
            });
        }

        @Override
        public void onFailed(final int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String msg = String.format(operationName + " failed, reason = %d", reason);
                    log(msg);
                    setSyncPanelEnabled(true);
                }
            });
        }
    }
}
