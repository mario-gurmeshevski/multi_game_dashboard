-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class com.example.educationgame.data.local.entity.** { *; }
-keep class com.example.educationgame.data.local.dao.** { *; }
-keep class com.example.educationgame.data.local.Converters { *; }
-keep class com.example.educationgame.data.enums.** { *; }

-dontwarn androidx.room.**
