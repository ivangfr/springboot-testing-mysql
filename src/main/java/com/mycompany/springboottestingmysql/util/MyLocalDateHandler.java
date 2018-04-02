package com.mycompany.springboottestingmysql.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MyLocalDateHandler {

    public static final String PATTERN_dd_MM_yyyy = "dd-MM-yyyy";
    public static final String ZONE_ID = "CET";

    public static Date fromStringToDate(String string) {
        return fromStringToDate(string, PATTERN_dd_MM_yyyy, ZONE_ID);
    }

    public static Date fromStringToDate(String string, String pattern, String zoneId) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        LocalDate ld = LocalDate.parse(string, dtf);
        ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of(zoneId));
        return Date.from(zdt.toInstant());
    }

    public static String fromDateToString(Date date) {
        return fromDateToString(date, PATTERN_dd_MM_yyyy, ZONE_ID);
    }

    public static String fromDateToString(Date date, String pattern, String zoneId) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        LocalDate ld = date.toInstant().atZone(ZoneId.of(zoneId)).toLocalDate();
        return ld.format(dtf);
    }

}
