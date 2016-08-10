package com.misfit.misfitsdkdemo;


import com.misfit.misfitsdk.enums.MFGender;
import com.misfit.misfitsdk.model.MFDeviceConfiguration;
import com.misfit.misfitsdk.model.MFGraphItem;
import com.misfit.misfitsdk.model.MFProfile;

import java.util.Calendar;

public class DataSource {
    public final static int MALE_RECOMMENDED_AGE = 31;
    public final static float MALE_RECOMMENDED_HEIGHT = 68.3f;
    public final static float MALE_RECOMMENDED_WEIGHT = 140.0f;

    public final static int FEMALE_RECOMMENDED_AGE = 29;
    public final static float FEMALE_RECOMMENDED_HEIGHT = 65.5f;
    public final static float FEMALE_RECOMMENDED_WEIGHT = 100.0f;

    public static MFProfile getDefaultMale() {
        return MFProfile.createByUSUnit(MFGender.MALE, MALE_RECOMMENDED_AGE, MALE_RECOMMENDED_HEIGHT, MALE_RECOMMENDED_WEIGHT);
    }

    public static long getTimeBeforeTwoHours(int hours) {
        long currTime = Calendar.getInstance().getTimeInMillis() / 1000;
//        return currTime - 60 * 60 * hours;
        return 0;
    }

    public static MFGraphItem getFakeGraphItem() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        long endTime = calendar.getTimeInMillis() / 1000;
        calendar.add(Calendar.MINUTE, -15);
        long startTime = calendar.getTimeInMillis() / 1000;
        return new MFGraphItem(startTime, endTime, 0);
    }

    public static MFDeviceConfiguration getConfig() {
        return MFDeviceConfiguration.createSetConfiguration(
                MFDeviceConfiguration.ClockState.CLOCK_STATE_DEFAULT,
                MFDeviceConfiguration.TripleTapState.TRIPLE_TAP_STATE_DEFAULT,
                MFDeviceConfiguration.ActivityTaggingState.ACTIVITY_TAGGING_DEFAULT,
                120,
                2000
        );
    }
}
