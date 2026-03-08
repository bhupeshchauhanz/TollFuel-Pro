package com.tollfuelpro.app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy 'at' h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
