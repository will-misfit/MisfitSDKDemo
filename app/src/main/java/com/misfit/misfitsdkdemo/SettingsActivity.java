package com.misfit.misfitsdkdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.misfit.misfitsdk.MFAdapter;
import com.misfit.misfitsdk.callback.MFOperationResultCallback;
import com.misfit.misfitsdk.device.MFDevice;
import com.misfit.misfitsdk.model.MFAlarmSettings;
import com.misfit.misfitsdk.model.MFSettingsParams;
import com.misfit.misfitsdk.model.SupportedFeature;
import com.misfit.misfitsdkdemo.view.RangePreference;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    private final static String TAG = "SettingsActivity";
    private final static String EXTRA_SERIAL = "extra_serial";

    @BindView(R.id.sw_clear)
    Switch mAlarmClearSw;
    @BindView(R.id.prf_hour)
    RangePreference mAlarmHour;
    @BindView(R.id.prf_min)
    RangePreference mAlarmMin;
    @BindView(R.id.prf_smart_window)
    RangePreference mAlarmSmartWindow;
    @BindView(R.id.btn_repeat)
    Button mAlarmRepeatBtn;
    @BindView(R.id.sw_alarm)
    Switch mAlarmSw;
    @BindView(R.id.sw_call_text)
    Switch mCallTextSw;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_set) {
            writeSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void writeSettings() {
        MFSettingsParams settingsParams = new MFSettingsParams();
        if (mAlarmSw.isChecked() && mDevice.hasFeature(SupportedFeature.ALARM)) {
            if (mAlarmClearSw.isChecked()) {
                settingsParams.shouldClearAllAlarms = true;
            } else {
                int time = mAlarmHour.getValue() * 3600 + mAlarmMin.getValue() * 60;
                List<MFAlarmSettings> alarms = new ArrayList<>();
                alarms.add(new MFAlarmSettings(time, mCurrentRepeatType, mAlarmSmartWindow.getValue()));
                settingsParams.alarmSettings = alarms;
            }
        }
        if (mDevice.hasFeature(SupportedFeature.CALL_TEXT_NOTIFICATION)) {
            settingsParams.isCallTextNotificationEnabled = mCallTextSw.isEnabled();
        }
        mDevice.writeSettings(settingsParams, new MFOperationResultCallback() {
            @Override
            public void onSucceed() {
                Log.i(TAG, "write settings succeed");
            }

            @Override
            public void onFailed(int i) {
                Log.i(TAG, "write settings failed");
            }
        });
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
}
