package com.example.aliyunlivedemo.util;

/**
 * @Author: lifujun@alibaba-inc.com
 * @Date: 2016/12/29.
 * @Description:
 */

public class Formatter {

    public static String formatTime(int ms) {
        int totalSeconds = ms / 1000;
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60 % 60;
        int hours = totalSeconds / 60 / 60;
        String timeStr = "";
        if (hours > 9) {
            timeStr += hours + ":";
        } else if (hours > 0) {
            timeStr += "0" + hours + ":";
        }
        if (minutes > 9) {
            timeStr += minutes + ":";
        } else if (minutes > 0) {
            timeStr += "0" + minutes + ":";
        } else {
            timeStr += "00:";
        }
        if (seconds > 9) {
            timeStr += seconds;
        } else if (seconds > 0) {
            timeStr += "0" + seconds;
        } else {
            timeStr += "00";
        }

        return timeStr;
    }
}
