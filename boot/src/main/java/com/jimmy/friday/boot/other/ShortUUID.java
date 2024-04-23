package com.jimmy.friday.boot.other;

import java.util.concurrent.atomic.AtomicInteger;

public class ShortUUID {
    private static final String[] l = {"0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z", "#", "@", "[", "]", "^", ")", "*", "(",
            "-", "+", "/", "=", ",", ".", "?", "/", ":", ";",
            "\\", "{", "}", "|", "~", "`"};

    private static AtomicInteger count = new AtomicInteger(1000);

    public static int getCount() {
        count.compareAndSet(9999, 1000);
        return count.incrementAndGet();
    }

    // TentoN(这里是你想转换的数 ,这里是你想转换为多少进制 2-62之间）
    public static String TentoN(long value, int number) {
        if (number <= 1 || number > l.length) {
            throw new RuntimeException("Faild");
        }
        // 负数处理
        if (value < 0) {
            return "-" + TentoN(0 - value, number);
        }
        if (value < number) {
            return l[(int) value];
        } else {
            long n = value % (long) number;
            return (TentoN(value / number, number) + l[(int) n]);
        }
    }

    public static String uuid() {
        return TentoN((System.currentTimeMillis()), 36) + TentoN(getCount(), 36);
    }
}
