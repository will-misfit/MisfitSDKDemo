package com.misfit.misfitsdkdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.misfit.misfitsdk.MFAdapter;
import com.misfit.misfitsdk.callback.MFOperationResultCallback;
import com.misfit.misfitsdk.device.MFDevice;
import com.misfit.misfitsdk.model.MFAlarmSettings;
import com.misfit.misfitsdk.model.MFInactivityNudgeSettings;
import com.misfit.misfitsdk.model.SupportedFeature;
import com.misfit.misfitsdkdemo.view.RangePreference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    private final static String TAG = "SettingsActivity";
    private final static String EXTRA_SERIAL = "extra_serial";

    @BindView(R.id.prf_hour)
    RangePreference mAlarmHour;
    @BindView(R.id.prf_min)
    RangePreference mAlarmMin;
    @BindView(R.id.btn_repeat)
    Button mAlarmRepeatBtn;

    @BindView(R.id.prf_nudge_start_hour)
    RangePreference mNudgeStartHour;
    @BindView(R.id.prf_nudge_start_min)
    RangePreference mNudgeStartMin;
    @BindView(R.id.prf_nudge_end_hour)
    RangePreference mNudgeEndHour;
    @BindView(R.id.prf_nudge_end_min)
    RangePreference mNudgeEndMin;
    @BindView(R.id.prf_nudge_repeat_min)
    RangePreference mNudgeRepeatMin;

    ProgressDialog mProgressDialog;

    private MFDevice mDevice;

    private MFAlarmSettings.RepeatType mCurrentRepeatType = MFAlarmSettings.RepeatType.DAILY;
    private MFAlarmSettings.RepeatType[] mRepeatSelections = new MFAlarmSettings.RepeatType[]{
            MFAlarmSettings.RepeatType.DAILY,
            MFAlarmSettings.RepeatType.NEVER
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        String serial = getIntent().getStringExtra(EXTRA_SERIAL);
        if (TextUtils.isEmpty(serial)) {
            Toast.makeText(this, "Serial number is illegal", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mDevice = MFAdapter.getInstance().getDevice(serial);
        mAlarmRepeatBtn.setText(mCurrentRepeatType.name());
    }

    @OnClick(R.id.btn_set_alarm)
    void handleAlarmSettings() {
        if (!mDevice.hasFeature(SupportedFeature.ALARM)) {
            toast("Device doesn't have alarm feature");
            return;
        }
        playProgressDialog();
        int time = mAlarmHour.getValue() * 3600 + mAlarmMin.getValue() * 60;
        mDevice.setSingleAlarm(new MFAlarmSettings(time, mCurrentRepeatType), new OperationCallback("set alarm"));
    }

    @OnClick(R.id.btn_clear_alarms)
    void clearAlarms() {
        if (!mDevice.hasFeature(SupportedFeature.ALARM)) {
            toast("Device doesn't have alarm feature");
            return;
        }
        playProgressDialog();
        mDevice.clearAllAlarms(new OperationCallback("Clear alarms"));
    }

    @OnClick(R.id.btn_enable_inactivity_nudge)
    void enableInActivityNudgeSettings() {
        if (!mDevice.hasFeature(SupportedFeature.INACTIVITY_NUDGE)) {
            toast("Device doesn't have inactivity_nudge feature");
            return;
        }
        playProgressDialog();
        int startHour = mNudgeStartHour.getValue();
        int startMin = mNudgeStartMin.getValue();
        int endHour = mNudgeEndHour.getValue();
        int endMin = mNudgeEndMin.getValue();
        int repeatMin = mNudgeRepeatMin.getValue();

        MFInactivityNudgeSettings nudgeSettings = new MFInactivityNudgeSettings(true, startHour, startMin, endHour, endMin, repeatMin);
        mDevice.setInactivityNudge(nudgeSettings, new OperationCallback("Enable InactivityNudge"));
    }

    @OnClick(R.id.btn_disable_inactivity_nudge)
    void disableInActivityNudgeSettings() {
        if (!mDevice.hasFeature(SupportedFeature.INACTIVITY_NUDGE)) {
            toast("Device doesn't have inactivity_nudge feature");
            return;
        }
        playProgressDialog();

        MFInactivityNudgeSettings nudgeSettings = new MFInactivityNudgeSettings(false, 0, 0, 0, 0, 0);
        mDevice.setInactivityNudge(nudgeSettings, new OperationCallback("Disable InactivityNudge"));
    }

    @OnClick(R.id.btn_repeat)
    void onClickRepeat() {
        String[] repeatTexts = new String[mRepeatSelections.length];
        for (int i = 0; i < mRepeatSelections.length; i++) {
            repeatTexts[i] = mRepeatSelections[i].name();
        }
        DialogUtils.showSlectionDialog(this,
                repeatTexts,
                "choose repeat mode",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mCurrentRepeatType = mRepeatSelections[i];
                        mAlarmRepeatBtn.setText(mCurrentRepeatType.name());
                    }
                }
        );
    }

    public static Intent getOpenIntent(Context context, String serialNumber) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(EXTRA_SERIAL, serialNumber);
        return intent;
    }

    private void playProgressDialog() {
        if (mProgressDialog != null) {
            return;
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.show();
    }

    private void displayProgressDialog() {
        if (mProgressDialog == null) {
            return;
        }
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }

    private void toast(final String prompt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingsActivity.this, prompt, Toast.LENGTH_SHORT).show();
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
                    String msg = operationName + " success";
                    Log.d(TAG, msg);
                    toast(msg);
                    displayProgressDialog();
                }
            });
        }

        @Override
        public void onFailed(final int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String msg = String.format(operationName + " failed, reason = %d", reason);
                    Log.d(TAG, msg);
                    toast(msg);
                    displayProgressDialog();
                }
            });
        }
    }
}
