package com.misfit.misfitsdkdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.misfit.misfitsdk.MFAdapter;
import com.misfit.misfitsdk.Version;
import com.misfit.misfitsdk.callback.MFBleCallback;
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
import com.misfit.misfitsdk.model.MFAlarmSettings;
import com.misfit.misfitsdk.model.MFDeviceInfo;
import com.misfit.misfitsdk.model.MFGraphItem;
import com.misfit.misfitsdk.model.MFInactivityNudgeSettings;
import com.misfit.misfitsdk.model.MFSleepSession;
import com.misfit.misfitsdk.model.MFSyncParams;
import com.misfit.misfitsdk.model.SupportedFeature;
import com.misfit.misfitsdk.utils.MLog;
import com.misfit.misfitsdkdemo.view.RangePreference;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SyncActivity extends AppCompatActivity {

    private final static String TAG = "SyncActivity";

    private final static int REQ_SCAN = 1;
    @BindView(R.id.btn_stop_listen_gesture)
    Button mBtnStopListenGesture;

    @BindView(R.id.text_serial_number)
    TextView mTextSerialNumber;
    @BindView(R.id.text_device_name)
    TextView mTextDeviceName;
    @BindViews({R.id.btn_sync,
            R.id.btn_get_config,
            R.id.btn_play_call,
            R.id.btn_play_text,
            R.id.btn_stop_animation,
            R.id.btn_map_button,
            R.id.btn_start_scan,
            R.id.btn_stop_scan,
            R.id.btn_debug_sync,
            R.id.btn_hid_connect,
            R.id.btn_hid_disconnect,
            R.id.btn_play_animation,
            R.id.btn_unmap_all,
            R.id.btn_start_listen_gesture,
            R.id.btn_stop_listen_gesture,
            R.id.btn_set_goal,
            R.id.btn_get_mapping,
            R.id.btn_alarm,
            R.id.btn_inactivity_nudge,
            R.id.btn_map_activity_tagging})
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

    private MFGesture[] mRayGesture = new MFGesture[]{
            MFGesture.RAY_TRIPLE_TAP

    };

    private MFGesture[] mShine2Gesture = new MFGesture[]{
            MFGesture.SHINE2_TRIPLE_TAP

    };
    private MFDevice mDevice;

    private MFHIdConnectionCallback mHidCallback = new MFHIdConnectionCallback() {
        @Override
        public void onConnectionStateChange(String serialNumber, int state) {
            log(serialNumber + " connection state changed, new state=" + state);
        }
    };

    private Map<View, SupportedFeature> featureButtons;
    public StringBuilder mLogBuffer = new StringBuilder();
    private AlertDialog mLogDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        ButterKnife.bind(this);
        initButtons();
        setSyncPanelEnabled(false);

        MFAdapter sdkAdapter = MFAdapter.getInstance();
        sdkAdapter.init(this.getApplicationContext(),
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                "5c203ef8-d62a-11e5-ab30-625662870761");
    }

    private void initButtons() {
        featureButtons = new HashMap<>();
        featureButtons.put(findViewById(R.id.btn_play_animation), SupportedFeature.PLAY_ANIMATION);
        featureButtons.put(findViewById(R.id.btn_stop_animation), SupportedFeature.STOP_ANIMATION);
        featureButtons.put(findViewById(R.id.btn_play_call), SupportedFeature.CALL_TEXT_NOTIFICATION);
        featureButtons.put(findViewById(R.id.btn_play_text), SupportedFeature.CALL_TEXT_NOTIFICATION);
        featureButtons.put(findViewById(R.id.btn_hid_connect), SupportedFeature.HID);
        featureButtons.put(findViewById(R.id.btn_hid_disconnect), SupportedFeature.HID);
        featureButtons.put(findViewById(R.id.btn_map_button), SupportedFeature.MAP_BUTTON);
        featureButtons.put(findViewById(R.id.btn_map_activity_tagging), SupportedFeature.MAP_ACTIVITY_TAGGING);
        featureButtons.put(findViewById(R.id.btn_unmap_all), SupportedFeature.UNMAP_ALL);
        featureButtons.put(findViewById(R.id.btn_get_mapping), SupportedFeature.GET_MAPPING_TYPE);
        featureButtons.put(findViewById(R.id.btn_start_listen_gesture), SupportedFeature.LISTEN_GESTURE);
        featureButtons.put(findViewById(R.id.btn_stop_listen_gesture), SupportedFeature.LISTEN_GESTURE);
        featureButtons.put(findViewById(R.id.btn_alarm), SupportedFeature.ALARM);
        featureButtons.put(findViewById(R.id.btn_inactivity_nudge), SupportedFeature.INACTIVITY_NUDGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_log) {
            if (mLogDialog != null && mLogDialog.isShowing()) {
                mLogDialog.dismiss();
            }
            mLogDialog = new AlertDialog.Builder(this)
                    .setTitle("Logs")
                    .setMessage(mLogBuffer.toString())
                    .show();
            return true;
        }
        if (item.getItemId() == R.id.action_about) {
            String versionInfo = String.format("SyncSDK-%s, SyncDemo-%s",
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
                                MLog.i("outside", "found device=" + device.getSerialNumber());
                            }

                            @Override
                            public void onScanFailed(@MFDefine.ScanFailedReason int reason) {
                                MLog.i("outside", "failed=" + reason);
                            }
                        });
                    }
                }).show();
    }

    @OnClick(R.id.btn_rssi)
    void readRssi() {
        if (mDevice != null) {
            boolean result = mDevice.readRssi(new MFBleCallback() {
                @Override
                public void onRssiRead(final int status, final int rssi) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log(String.format(Locale.US, "status=%d, rssi=%d", status, rssi));
                        }
                    });
                }
            });

            if (!result) {
                Toast.makeText(this, "ReadRssi return false", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No device instance exist", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_alarm)
    void setSingleAlarm() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_single_alarm, null);
        final RangePreference hour = (RangePreference) view.findViewById(R.id.prf_hour);
        final RangePreference min = (RangePreference) view.findViewById(R.id.prf_min);
        final RangePreference repeat = (RangePreference) view.findViewById(R.id.prf_repeat);
        final MFAlarmSettings.RepeatType[] repeatTypes = new MFAlarmSettings.RepeatType[]{
                MFAlarmSettings.RepeatType.NEVER,
                MFAlarmSettings.RepeatType.DAILY
        };
        String[] repeatStrings = new String[repeatTypes.length];
        for (int i = 0; i < repeatTypes.length; i++) {
            repeatStrings[i] = repeatTypes[i].name();
        }
        repeat.setValues(repeatStrings);

        new AlertDialog.Builder(this)
                .setTitle("Set single alarm")
                .setView(view)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int timeInSec = hour.getValue() * 60 * 60 + min.getValue() * 60;
                        MFAlarmSettings alarmSettings = new MFAlarmSettings(timeInSec, repeatTypes[repeat.getValue()]);
                        mDevice.setSingleAlarm(alarmSettings, new OperationCallback("Set single alarm"));
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDevice.clearAllAlarms(new OperationCallback("Clear alarms"));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @OnClick(R.id.btn_inactivity_nudge)
    void setInactivityNudge() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_inactivity_nudge, null);
        final RangePreference startHour = (RangePreference) view.findViewById(R.id.prf_nudge_start_hour);
        final RangePreference startMin = (RangePreference) view.findViewById(R.id.prf_nudge_start_min);
        final RangePreference endHour = (RangePreference) view.findViewById(R.id.prf_nudge_end_hour);
        final RangePreference endMin = (RangePreference) view.findViewById(R.id.prf_nudge_end_min);
        final RangePreference repeat = (RangePreference) view.findViewById(R.id.prf_nudge_repeat_min);

        new AlertDialog.Builder(this)
                .setTitle("Set inactivity nudge")
                .setView(view)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MFInactivityNudgeSettings settings = new MFInactivityNudgeSettings(true,
                                startHour.getValue(),
                                startMin.getValue(),
                                endHour.getValue(),
                                endMin.getValue(),
                                repeat.getValue());
                        mDevice.setInactivityNudge(settings, new OperationCallback("Enable inactivity nudge"));
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Disable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MFInactivityNudgeSettings settings = new MFInactivityNudgeSettings(false, 0, 0, 0, 0, 0);
                        mDevice.setInactivityNudge(settings, new OperationCallback("Disable inactivity nudge"));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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
        setSyncPanelEnabled(false);
        mDevice.getDeviceInfo(new MFGetCallback<MFDeviceInfo>() {
            @Override
            public void onGet(final MFDeviceInfo data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        log(Printer.getDeviceInfoText(data));
                    }
                });
            }
        }, new OperationCallback("getConfig"));
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
        mDevice.stopAnimation(new OperationCallback("stopAnimation"));
    }

    @OnClick(R.id.btn_play_call)
    void playCall() {
        mDevice.playCallNotification(new OperationCallback("playCallNotification"));
    }

    @OnClick(R.id.btn_play_text)
    void playText() {
        mDevice.playTextNotification(new OperationCallback("playTextNotification"));
    }

    @OnClick(R.id.btn_hid_connect)
    void hidConnect() {
        mDevice.connectHid(mHidCallback, new OperationCallback("hidConnect"));
    }

    @OnClick(R.id.btn_hid_disconnect)
    void hidDisConnect() {
        mDevice.disconnectHid(null);
    }

    @OnClick(R.id.btn_map_button)
    void mapButton() {

        if (mDevice.getDeviceType() == MFDeviceType.SHINE2) {
            mapButton(mShine2Gesture);
        } else if (mDevice.getDeviceType() == MFDeviceType.RAY) {
            mapButton(mRayGesture);
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
        setSyncPanelEnabled(false);
        mDevice.mapActivityTagging(new OperationCallback("mapActivityTagging"));
    }

    @OnClick(R.id.btn_get_mapping)
    void getMappingType() {
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

    @OnClick(R.id.btn_unmap_all)
    void unmapAll() {
        setSyncPanelEnabled(false);
        mDevice.unmapAll(new OperationCallback("unmapAll"));
    }

    @OnClick(R.id.btn_sync)
    void sync() {
        MFSyncParams syncParams = createSyncParams();
        setSyncPanelEnabled(false);
        mDevice.startSync(syncParams,
                new OperationCallback("sync"),
                new MFGetCallback<MFDeviceInfo>() {
                    @Override
                    public void onGet(final MFDeviceInfo data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                log(Printer.getDeviceInfoText(data));
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
    }

    @OnClick(R.id.btn_debug_sync)
    void debugSync() {
        new AlertDialog.Builder(this)
                .setTitle("WARMING")
                .setMessage("This operation is only for debugging. Using this operation in your production version may lead to serious problem.")
                .setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startDebugSync();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void startDebugSync() {
        MFSyncParams syncParams = createSyncParams();
        mDevice.debugSync(syncParams,
                new OperationCallback("debug sync"),
                new MFGetCallback<MFDeviceInfo>() {
                    @Override
                    public void onGet(final MFDeviceInfo data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                log(Printer.getDeviceInfoText(data));
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
        mDevice.playAnimation(new OperationCallback("playAnimation"));
    }

    @OnClick(R.id.btn_stop)
    void stop() {
        mDevice.stopOperation();
        setSyncPanelEnabled(true);
    }

    @OnClick(R.id.btn_start_listen_gesture)
    void startListenGesture() {
        mDevice.startListenGesture(
                new MFGestureCallback() {
                    @Override
                    public void onGestureReceived(MFGesture gesture) {
                        log("receive gesture:" + gesture.name());
                    }

                    @Override
                    public void onHeartbeatReceived() {
                        log("receive heart beat");
                    }
                },
                new OperationCallback("startListenGesture") {
                    @Override
                    public void onSucceed() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg = getOperationName() + " finished";
                                log(msg);
                                setSyncPanelEnabled(false);
                                mBtnStopListenGesture.setEnabled(true);
                            }
                        });
                    }
                });
    }

    @OnClick(R.id.btn_stop_listen_gesture)
    void stopListenGesture() {
        mDevice.stopListenGesture(new OperationCallback("stopListenGesture") {
            @Override
            public void onFailed(final int reason) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = String.format(getOperationName() + " failed, reason = %d", reason);
                        log(msg);
                        setSyncPanelEnabled(false);
                    }
                });
            }
        });
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
    }

    private void setSyncPanelEnabled(boolean enabled) {
        for (View view : syncPanel) {
            view.setEnabled(enabled);
        }
    }

    private MFSyncParams createSyncParams() {
        MFSyncParams MFSyncParams = new MFSyncParams(DataSource.getDefaultMale());
        MFSyncParams.lastGraphItem = DataSource.getFakeGraphItem();

        return MFSyncParams;
    }

    private void log(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, msg);
                mLogBuffer.append(msg)
                        .append("\n");
                if (mLogBuffer.length() > 4000) {
                    mLogBuffer.delete(0, mLogBuffer.length() - 4000);
                }
                if (mLogDialog != null && mLogDialog.isShowing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLogDialog.setMessage(mLogBuffer.toString());
                        }
                    });
                }
            }
        });
    }

    private class OperationCallback implements MFOperationResultCallback {
        private String operationName;

        public String getOperationName() {
            return operationName;
        }

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
