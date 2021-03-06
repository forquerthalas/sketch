package me.xiaopan.sketchsample.util

import android.content.Context
import android.preference.PreferenceManager
import me.xiaopan.sketch.BuildConfig
import me.xiaopan.sketchsample.event.AppConfigChangedEvent
import org.greenrobot.eventbus.EventBus

object AppConfig {

    fun getBoolean(context: Context, key: Key): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key.keyName, key.isDefaultValue)
    }

    fun putBoolean(context: Context, key: Key, newValue: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key.keyName, newValue).apply()
        EventBus.getDefault().post(AppConfigChangedEvent(key))
    }

    fun getString(context: Context, key: Key): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key.keyName, null)
    }

    fun putString(context: Context, key: Key, newValue: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key.keyName, newValue).apply()
        EventBus.getDefault().post(AppConfigChangedEvent(key))
    }

    enum class Key constructor(val keyName: String, val isDefaultValue: Boolean) {
        SCROLLING_PAUSE_LOAD("PREFERENCE_SCROLLING_PAUSE_LOAD", false),
        SHOW_IMAGE_DOWNLOAD_PROGRESS("PREFERENCE_SHOW_IMAGE_DOWNLOAD_PROGRESS", false),
        MOBILE_NETWORK_PAUSE_DOWNLOAD("PREFERENCE_MOBILE_NETWORK_PAUSE_DOWNLOAD", true),
        SHOW_IMAGE_FROM_FLAG("PREFERENCE_SHOW_IMAGE_FROM_FLAG", false),
        CLICK_RETRY_ON_PAUSE_DOWNLOAD("PREFERENCE_CLICK_DISPLAY_ON_PAUSE_DOWNLOAD", true),
        CLICK_RETRY_ON_FAILED("PREFERENCE_CLICK_DISPLAY_ON_FAILED", true),
        CLICK_SHOW_PRESSED_STATUS("PREFERENCE_CLICK_SHOW_PRESSED_STATUS", true),
        GLOBAL_DISABLE_CACHE_IN_MEMORY("PREFERENCE_GLOBAL_DISABLE_CACHE_IN_MEMORY", false),
        GLOBAL_DISABLE_CACHE_IN_DISK("PREFERENCE_GLOBAL_DISABLE_CACHE_IN_DISK", false),
        GLOBAL_DISABLE_BITMAP_POOL("PREFERENCE_GLOBAL_DISABLE_BITMAP_POOL", false),
        GLOBAL_LOW_QUALITY_IMAGE("PREFERENCE_GLOBAL_LOW_QUALITY_IMAGE", false),
        GLOBAL_IN_PREFER_QUALITY_OVER_SPEED("PREFERENCE_GLOBAL_IN_PREFER_QUALITY_OVER_SPEED", false),
        SUPPORT_ZOOM("PREFERENCE_SUPPORT_ZOOM", true),
        SUPPORT_HUGE_IMAGE("PREFERENCE_SUPPORT_HUGE_IMAGE", true),
        READ_MODE("PREFERENCE_READ_MODE", true),
        THUMBNAIL_MODE("PREFERENCE_THUMBNAIL_MODE", true),
        LOCATION_ANIMATE("PREFERENCE_LOCATION_ANIMATE", true),
        CACHE_PROCESSED_IMAGE("PREFERENCE_CACHE_PROCESSED_IMAGE", true),
        DISABLE_CORRECT_IMAGE_ORIENTATION("PREFERENCE_DISABLE_CORRECT_IMAGE_ORIENTATION", false),
        PAGE_VISIBLE_TO_USER_DECODE_HUGE_IMAGE("PREFERENCE_PAGE_VISIBLE_TO_USER_DECODE_HUGE_IMAGE", true),
        PLAY_GIF_ON_LIST("PREFERENCE_PLAY_GIF_ON_LIST", false),
        SHOW_GIF_FLAG("PREFERENCE_SHOW_GIF_FLAG", true),
        LOG_LEVEL("PREFERENCE_LOG_level", false),
        LOG_REQUEST("PREFERENCE_LOG_REQUEST", BuildConfig.DEBUG),
        LOG_CACHE("PREFERENCE_LOG_CACHE", false),
        LOG_ZOOM("PREFERENCE_LOG_ZOOM", false),
        LOG_HUGE_IMAGE("PREFERENCE_LOG_HUGE_IMAGE", false),
        LOG_TIME("PREFERENCE_LOG_TIME", false),
        SHOW_TOOLS_IN_IMAGE_DETAIL("PREFERENCE_SHOW_TOOLS_IN_IMAGE_DETAIL", false),
        OUT_LOG_2_SDCARD("PREFERENCE_OUT_LOG_2_SDCARD", false),
        CLICK_PLAY_GIF("PREFERENCE_CLICK_PLAY_GIF", false),
        SHOW_UNSPLASH_RAW_IMAGE("PREFERENCE_SHOW_UNSPLASH_RAW_IMAGE", false),
        LONG_CLICK_SHOW_IMAGE_INFO("LONG_CLICK_SHOW_IMAGE_INFO", false),
        SHOW_ROUND_RECT_IN_PHOTO_LIST("SHOW_ROUND_RECT_IN_PHOTO_LIST", false)
    }
}
