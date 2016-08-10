package com.misfit.misfitsdkdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.misfit.misfitsdk.MFAdapter;
import com.misfit.misfitsdk.Version;
import com.misfit.misfitsdk.bluetooth.shine.ShineConfiguration;
import com.misfit.misfitsdk.bluetooth.shine.controller.ConfigurationSession;
import com.misfit.misfitsdk.callback.MFDataOutPutCallback;
import com.misfit.misfitsdk.callback.MFOperationResultCallback;
import com.misfit.misfitsdk.callback.OnOtaListener;
import com.misfit.misfitsdk.callback.OnTagInStateListener;
import com.misfit.misfitsdk.callback.OnTagInUserInputListener;
import com.misfit.misfitsdk.device.MFDevice;
import com.misfit.misfitsdk.enums.MFDeviceType;
import com.misfit.misfitsdk.model.MFActivitySessionGroup;
import com.misfit.misfitsdk.model.MFGraphItem;
import com.misfit.misfitsdk.model.MFSleepSession;
import com.misfit.misfitsdk.model.MFSyncParams;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SyncActivity extends AppCompatActivity
        implements OnOtaListener, MFDataOutPutCallback, MFOperationResultCallback {

    private final static String TAG = "SyncActivity";

    private final static int REQ_SCAN = 1;
    private final static int REQ_ALARM_SETTING = 2;
    private final static int REQ_INACTIVITY_NUDGE_SETTING = 3;
    private final static int REQ_NOTIFICATION_SETTING = 4;

    @Bind(R.id.text_serial_number)
    TextView mTextSerialNumber;
    @Bind(R.id.text_device_name)
    TextView mTextDeviceName;
    @Bind(R.id.btn_sync)
    Button mSyncButton;
    @Bind(R.id.switch_should_force_ota)
    Switch mSwitchShouldForceOta;
    @Bind(R.id.switch_activate)
    Switch mSwitchActivate;
    @Bind(R.id.switch_tagging_response)
    Switch mSwitchTaggingResponse;
    @Bind({R.id.btn_sync,
            R.id.btn_stop,
            R.id.switch_activate,
            R.id.switch_tagging_response,
            R.id.switch_should_force_ota})
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
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Gson mGson;
    private MFDevice mMFDevice;

    private OnTagInStateListener mTagInStateListener = new OnTagInStateListener() {
        @Override
        public void onDeviceTaggingIn(int deviceType, OnTagInUserInputListener inputCallback) {
            inputCallback.onUserInputForTaggingIn(mSwitchTaggingResponse.isChecked());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        ButterKnife.bind(this);
        mGson = new GsonBuilder().setPrettyPrinting().create();
        setSyncPanelEnabled(false);

        MFAdapter sdkAdapter = MFAdapter.getInstance();
        sdkAdapter.init(this.getApplicationContext(),
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                "5c203ef8-d62a-11e5-ab30-625662870761");
//        OkHttpClient client = new OkHttpClient.Builder().build();
//        Log.i(TAG, client.toString());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                        Log.i(TAG, String.format("start scan, device type is %s ", MFDeviceType.getDeviceTypeText(selectedDeviceType)));
                        startActivityForResult(intent, REQ_SCAN);
                    }
                }).show();
    }

    @OnClick(R.id.btn_sync)
    void sync() {
        MFSyncParams MFSyncParams = createSyncParams();
        mMFDevice.startSync(this, this, this, MFSyncParams);
        setSyncPanelEnabled(false);
    }

    @OnClick(R.id.btn_play_animation)
    void playAnimation() {
        mMFDevice.playAnimation(this);
    }

    @OnClick(R.id.btn_stop)
    void stop() {
        mMFDevice.stopOperation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Log.d(TAG, String.format("result not ok, req = %d, result = %d", requestCode, resultCode));
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
        Log.d(TAG, "updated device, serialNumber=" + serialNumber);
        mMFDevice = MFAdapter.getInstance().getDevice(serialNumber);
        mTextSerialNumber.setText(serialNumber);
        mTextDeviceName.setText(MFDeviceType.getDeviceTypeText(serialNumber));
        mSwitchTaggingResponse.setVisibility(MFDeviceType.getDeviceType(serialNumber) == MFDeviceType.FLASH ? View.VISIBLE : View.GONE);
    }

    private void setSyncPanelEnabled(boolean enabled) {
        for (View view : syncPanel) {
            view.setEnabled(enabled);
        }
    }

    /* interface methods of OnOtaListener */
    @Override
    public void onEntireOtaCompleted() {
        Log.d(TAG, "entire OTA Completed");
    }

    @Override
    public boolean isForceOta(boolean hasNewFirmware) {
        return mSwitchShouldForceOta.isChecked();
    }

    /* interface methods of MFDataOutPutCallback */

    @Override
    public int onActivitySessionGroupSynced(MFActivitySessionGroup activitySessionGroup) {
        Log.i(TAG, Printer.getActivitySessionText(activitySessionGroup.getActivitySessions()));
        Log.i(TAG, Printer.getGapSessionText(activitySessionGroup.getGapSessions()));
        return 0;
    }

    @Override
    public void onSleepSessionSynced(List<MFSleepSession> MFSleepSessions) {
        Log.i(TAG, Printer.getSleepSessionText(MFSleepSessions));
    }

    @Override
    public void onGraphItemSynced(List<MFGraphItem> MFGraphItems) {
        Log.i(TAG, Printer.getGraphItemText(MFGraphItems));
    }

    @Override
    public void onGetShineConfigurationCompleted(final ConfigurationSession configSession) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, mGson.toJson(configSession));
            }
        });
    }

    private MFSyncParams createSyncParams() {
        MFSyncParams MFSyncParams = new MFSyncParams(DataSource.getDefaultMale(),
                DataSource.getTimeBeforeTwoHours(2),
                mSwitchActivate.isChecked(),
                mSwitchShouldForceOta.isChecked());
        MFSyncParams.tagInStateListener = mTagInStateListener;  //for flash
        MFSyncParams.deviceConfiguration = new ShineConfiguration();
        MFSyncParams.lastGraphItem = DataSource.getFakeGraphItem();

        return MFSyncParams;
    }

    @Override
    public void onSucceed() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "operation finished");
                setSyncPanelEnabled(true);
            }
        });
    }

    @Override
    public void onFailed(final int reason) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, String.format("operation failed, reason = %d", reason));
                setSyncPanelEnabled(true);
            }
        });
    }
}
