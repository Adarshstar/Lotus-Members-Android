-keep class com.lotus.members.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
