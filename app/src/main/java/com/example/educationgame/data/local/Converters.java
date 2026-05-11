package com.example.educationgame.data.local;

import androidx.room.TypeConverter;

import com.example.educationgame.data.enums.GameTypeEnum;

import java.util.Date;

public class Converters {

    @TypeConverter
    public static GameTypeEnum fromGameTypeString(String value) {
        return value == null ? null : GameTypeEnum.valueOf(value);
    }

    @TypeConverter
    public static String gameTypeToString(GameTypeEnum type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
