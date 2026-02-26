package com.socialmedia.utils;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class TimeAgo {
    public static String from(Timestamp ts) {
        if (ts == null) return "";
        Duration d = Duration.between(ts.toInstant(), Instant.now());

        long sec = d.getSeconds();
        if (sec < 60) return "Just now";
        long min = sec / 60;
        if (min < 60) return min + "min";
        long hrs = min / 60;
        if (hrs < 24) return hrs + "h";
        long days = hrs / 24;
        if (days < 7) return days + "d";
        long weeks = days / 7;
        if (weeks < 4) return weeks + "w";
        long months = days / 30;
        if (months < 12) return months + "mo";
        long years = days / 365;
        return years + "y";
    }
}