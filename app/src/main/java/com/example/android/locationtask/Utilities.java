package com.example.android.locationtask;

/**
 * Created by ramandeepbedi on 12/04/2017.
 */

public class Utilities {

    public static String formatSeconds(double timeInSeconds) {

        double timeInHours;
        double minuteTime;
        double secondTime;
        int hours;
        int minutes;
        int seconds;

        timeInHours = timeInSeconds / 3600;

        if (timeInSeconds >= 3600) {

            hours = Integer.parseInt((timeInHours + "").substring(0, 1));
            minuteTime = (timeInHours - hours) * 60;

            if (minuteTime >= 10) {
                minutes = Integer.parseInt((minuteTime + "").substring(0, 2));
            } else {
                minutes = Integer.parseInt((minuteTime + "").substring(0, 1));
            }

            secondTime = (minuteTime - minutes) * 60;
            if (secondTime >= 10) {
                seconds = Integer.parseInt((secondTime + "").substring(0, 2));
            } else {
                seconds = Integer.parseInt((secondTime + "").substring(0, 1));
            }

            return String.format("%sh %sm %ss", hours, minutes, seconds);

        } else {
            minuteTime = (timeInHours) * 60;

            if (minuteTime >= 10) {
                minutes = Integer.parseInt((minuteTime + "").substring(0, 2));
            } else {
                minutes = Integer.parseInt((minuteTime + "").substring(0, 1));
            }

            secondTime = (minuteTime - minutes) * 60;
            if (secondTime >= 10) {
                seconds = Integer.parseInt((secondTime + "").substring(0, 2));
            } else {
                seconds = Integer.parseInt((secondTime + "").substring(0, 1));
            }
            return String.format("%sm %ss", minutes, seconds);
        }

    }

}
