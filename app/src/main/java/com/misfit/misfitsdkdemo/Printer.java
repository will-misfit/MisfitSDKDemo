package com.misfit.misfitsdkdemo;


import com.misfit.misfitsdk.model.MFActivitySession;
import com.misfit.misfitsdk.model.MFDeviceInfo;
import com.misfit.misfitsdk.model.MFGapSession;
import com.misfit.misfitsdk.model.MFGraphItem;
import com.misfit.misfitsdk.model.MFSleepSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

public class Printer {

    private final static String DIVIDER = "*******************************************";
    private final static String WRAP = "\n";

    public static String getActivitySessionText(List<MFActivitySession> activitySessions) {
        StringBuilder builder = new StringBuilder(DIVIDER);
        builder.append("\nActivity session | size=")
                .append(getSize(activitySessions))
                .append(WRAP)
                .append(DIVIDER)
                .append(WRAP);
        for (MFActivitySession session : activitySessions) {
            builder.append(String.format("type=%d, start=%d, duration=%d, point=%d, step=%d", session.getType(), session.getStartTime(), session.getDuration(), session.getPoint(), session.getStep()))
                    .append(WRAP);
        }
        return builder.toString();
    }

    public static String getGapSessionText(List<MFGapSession> gapSessions) {
        StringBuilder builder = new StringBuilder(DIVIDER);
        builder.append("\nGap session | size=")
                .append(getSize(gapSessions))
                .append(WRAP)
                .append(DIVIDER)
                .append(WRAP);
        for (MFGapSession session : gapSessions) {
            builder.append(String.format("start=%d, duration=%d, point=%d, step=%d", session.getStartTime(), session.getDuration(), session.getPoint(), session.getStep()))
                    .append(WRAP);
        }
        return builder.toString();
    }

    public static String getSleepSessionText(List<MFSleepSession> sleepSessions) {
        StringBuilder builder = new StringBuilder(DIVIDER);
        builder.append("\nSleep session | size=")
                .append(getSize(sleepSessions))
                .append(WRAP)
                .append(DIVIDER)
                .append(WRAP);
        for (MFSleepSession session : sleepSessions) {
            builder.append(String.format("start=%d, duration=%d, deepSleep=%d", session.getStartTime(), session.getDuration(), session.getDeepSleepMinute()))
                    .append(WRAP);
        }
        return builder.toString();
    }

    public static String getGraphItemText(List<MFGraphItem> graphItems) {
        StringBuilder builder = new StringBuilder(DIVIDER);
        builder.append("\nGraph item | size=")
                .append(getSize(graphItems))
                .append(WRAP)
                .append(DIVIDER)
                .append(WRAP);
        for (MFGraphItem item : graphItems) {
            builder.append(String.format("start=%d, avePoint=%f", item.getStartTime(), item.getAveragePoint()))
                    .append(WRAP);
        }
        return builder.toString();
    }

    public static String getDeviceInfoText(MFDeviceInfo info) {
        JSONObject json = new JSONObject();
        String str = "";
        try {
            json.put("timestamp", info.getTimestampInSec());
            json.put("millisecond", info.getMilliSecond());
            json.put("battery", info.getBatteryLevel());
            json.put("goal", info.getGoalValue());
            json.put("todayPoint", info.getActivityPoint());
            json.put("taggingState", info.getActivityTaggingState());
            str = json.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return str;
    }


    private static int getSize(Collection collection) {
        if (collection == null || collection.isEmpty()) {
            return 0;
        } else {
            return collection.size();
        }
    }
}
