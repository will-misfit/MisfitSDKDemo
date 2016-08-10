package com.misfit.misfitsdkdemo;


import com.misfit.misfitsdk.model.MFActivitySession;
import com.misfit.misfitsdk.model.MFGapSession;
import com.misfit.misfitsdk.model.MFGraphItem;
import com.misfit.misfitsdk.model.MFSleepSession;

import java.util.List;

public class Printer {

    private final static String DIVIDER = "*******************************************";

    public static String getActivitySessionText(List<MFActivitySession> activitySessions) {
        StringBuilder builder = new StringBuilder(DIVIDER);
        builder.append("\nActivity session\n")
                .append(DIVIDER)
                .append("\n");
        for (MFActivitySession session : activitySessions) {
            builder.append(String.format("type=%d, start=%d, duration=%d, point=%d, step=%d", session.getType(), session.getStartTime(), session.getDuration(), session.getPoint(), session.getStep()))
                    .append("\n");
        }
        builder.append(DIVIDER);
        return builder.toString();
    }

    public static String getGapSessionText(List<MFGapSession> gapSessions) {
        StringBuilder builder = new StringBuilder(DIVIDER);
        builder.append("\nGap session\n")
                .append(DIVIDER)
                .append("\n");
        for (MFGapSession session : gapSessions) {
            builder.append(String.format("start=%d, duration=%d, point=%d, step=%d", session.getStartTime(), session.getDuration(), session.getPoint(), session.getStep()))
                    .append("\n");
        }
        builder.append(DIVIDER);
        return builder.toString();
    }

    public static String getSleepSessionText(List<MFSleepSession> sleepSessions) {
        StringBuilder builder = new StringBuilder(DIVIDER);
        builder.append("\nSleep session\n")
                .append(DIVIDER)
                .append("\n");
        for (MFSleepSession session : sleepSessions) {
            builder.append(String.format("start=%d, duration=%d, deepSleep=%d", session.getStartTime(), session.getDuration(), session.getDeepSleepMinute()))
                    .append("\n");
        }
        builder.append(DIVIDER);
        return builder.toString();
    }

    public static String getGraphItemText(List<MFGraphItem> graphItems) {
        StringBuilder builder = new StringBuilder(DIVIDER);
        builder.append("\nGraph item\n")
                .append(DIVIDER)
                .append("\n");
        for (MFGraphItem item : graphItems) {
            builder.append(String.format("start=%d, avePoint=%f", item.getStartTime(), item.getAveragePoint()))
                    .append("\n");
        }
        builder.append(DIVIDER);
        return builder.toString();
    }
}
