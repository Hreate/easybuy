package com.easybuy.system.util;

import java.util.Arrays;

public class IPv6PrefixConvert {
    private enum DIGITS {
        LEAST, MIDDLE, MOST
    }
    public static Integer[] convertPrefix(String digits) {
        var flag = DIGITS.MOST;
        if ("32".equals(digits)) {
            flag = DIGITS.LEAST;
        } else if ("48".equals(digits)) {
            flag = DIGITS.MIDDLE;
        }
        var list = switch (flag) {
            case LEAST -> Arrays.asList(65535, 65535, 0, 0, 0, 0, 0, 0);
            case MIDDLE -> Arrays.asList(65535, 65535, 65535, 0, 0, 0, 0, 0);
            case MOST -> Arrays.asList(65535, 65535, 65535, 65535, 0, 0, 0, 0);
        };
        return list.toArray(new Integer[0]);
    }
}



