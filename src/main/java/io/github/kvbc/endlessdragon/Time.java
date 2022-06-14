package io.github.kvbc.endlessdragon;

import java.time.LocalTime;

public final class Time {
    long hour;
    long minute;
    long second;

    public Time () {
        LocalTime time = LocalTime.now();
        this.hour = time.getHour();
        this.minute = time.getMinute();
        this.second = time.getSecond();
    }

    public Time (long hour, long minute, long second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public long ticks () {
        long seconds = this.hour * 60 * 60 + this.minute * 60 + this.second;
        return seconds * 20; // there's 20 ticks in 1 second
    }

    public Time add (Time time) {
        return new Time(
            this.hour + time.hour,
            this.minute + time.minute,
            this.second + time.second
        );
    }

    public Time diff (Time time) {
        return new Time(
            Math.abs(this.hour - time.hour),
            Math.abs(this.minute - time.minute),
            Math.abs(this.second - time.second)
        );
    }

    public String toTimeString () {
        return String.format("%02d", this.hour) + ':' +
               String.format("%02d", this.minute);
    }

    public String toDurationString () {
        String r = "";
        boolean h = (this.hour > 0);
        boolean m = (this.minute > 0);
        boolean s = (this.second > 0);
        if (h) {
            r += this.hour + "h";
            if (m || s) r += " ";
        }
        if (m) {
            r += this.minute + "m";
            if (s) r += " ";
        }
        if (s) {
            r += this.second + "s";
        }
        return r;
    }
}
