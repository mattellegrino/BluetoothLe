package com.example.bluetooth.le;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class BLETypeConversions {

    public static final int TZ_FLAG_NONE = 0;
    public static final int TZ_FLAG_INCLUDE_DST_IN_TZ = 1;

    public static byte[] join(byte[] start, byte[] end) {
        if (start == null || start.length == 0) {
            return end;
        }
        if (end == null || end.length == 0) {
            return start;
        }

        byte[] result = new byte[start.length + end.length];
        System.arraycopy(start, 0, result, 0, start.length);
        System.arraycopy(end, 0, result, start.length, end.length);
        return result;
    }

    public static byte[] calendarToRawBytes(Calendar timestamp) {
        // MiBand2:
        // year,year,month,dayofmonth,hour,minute,second,dayofweek,0,0,tz

        byte[] year = fromUint16(timestamp.get(Calendar.YEAR));
        return new byte[] {
                year[0],
                year[1],
                fromUint8(timestamp.get(Calendar.MONTH) + 1),
                fromUint8(timestamp.get(Calendar.DATE)),
                fromUint8(timestamp.get(Calendar.HOUR_OF_DAY)),
                fromUint8(timestamp.get(Calendar.MINUTE)),
                fromUint8(timestamp.get(Calendar.SECOND)),
                dayOfWeekToRawBytes(timestamp),
                0, // fractions256 (not set)
                // 0 (DST offset?) Mi2
                // k (tz) Mi2
        };
    }

    public static byte mapTimeZone(TimeZone timeZone) {
        int offsetMillis = timeZone.getRawOffset();
        int utcOffsetInQuarterHours = (offsetMillis / (1000 * 60 * 15));
        return (byte) utcOffsetInQuarterHours;
    }
    public static byte mapTimeZone(Calendar calendar, int timezoneFlags) {
        int offsetMillis = calendar.getTimeZone().getRawOffset();
        if (timezoneFlags == TZ_FLAG_INCLUDE_DST_IN_TZ) {
            offsetMillis = calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
        }
        int utcOffsetInQuarterHours = (offsetMillis / (1000 * 60 * 15));
        return (byte) utcOffsetInQuarterHours;
    }


    public static byte[] shortCalendarToRawBytes(Calendar timestamp) {
        // MiBand2:
        // year,year,month,dayofmonth,hour,minute

        byte[] year = fromUint16(timestamp.get(Calendar.YEAR));
        return new byte[] {
                year[0],
                year[1],
                fromUint8(timestamp.get(Calendar.MONTH) + 1),
                fromUint8(timestamp.get(Calendar.DATE)),
                fromUint8(timestamp.get(Calendar.HOUR_OF_DAY)),
                fromUint8(timestamp.get(Calendar.MINUTE))
        };
    }

    public static GregorianCalendar createCalendar() {
        return new GregorianCalendar();
    }

    public static byte[] fromUint16(int value) {
        return new byte[] {
                (byte) (value & 0xff),
                (byte) ((value >> 8) & 0xff),
        };
    }

    public static byte[] fromUint32(int value) {
        return new byte[] {
                (byte) (value & 0xff),
                (byte) ((value >> 8) & 0xff),
                (byte) ((value >> 16) & 0xff),
                (byte) ((value >> 24) & 0xff),
        };
    }

    public static byte fromUint8(int value) {
        return (byte) (value & 0xff);
    }

    public static byte dayOfWeekToRawBytes(Calendar cal) {
        int calValue = cal.get(Calendar.DAY_OF_WEEK);
        switch (calValue) {
            case Calendar.SUNDAY:
                return 7;
            default:
                return (byte) (calValue - 1);
        }
    }

}
