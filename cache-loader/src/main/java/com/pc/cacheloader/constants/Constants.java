package com.pc.cacheloader.constants;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final String ZK_UPDATE_PATH = "/loader/modify_time";
    public static final String ZK_RISE_PATH = "/loader/rise_time";
    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
