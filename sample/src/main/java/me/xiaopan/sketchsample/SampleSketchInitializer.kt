package me.xiaopan.sketchsample

import android.content.Context
import me.xiaopan.sketch.Configuration
import me.xiaopan.sketch.Initializer
import me.xiaopan.sketch.SLog
import me.xiaopan.sketchsample.event.AppConfigChangedEvent
import me.xiaopan.sketchsample.util.AppConfig
import me.xiaopan.sketchsample.util.XpkIconUriModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SampleSketchInitializer : Initializer {

    private var context: Context? = null
    private var configuration: Configuration? = null

    override fun onInitialize(context: Context, configuration: Configuration) {
        this.context = context
        this.configuration = configuration

        initConfig()

        EventBus.getDefault().register(this)
    }

    private fun initConfig() {
        onEvent(AppConfigChangedEvent(AppConfig.Key.OUT_LOG_2_SDCARD))
        onEvent(AppConfigChangedEvent(AppConfig.Key.LOG_LEVEL))
        onEvent(AppConfigChangedEvent(AppConfig.Key.LOG_TIME))
        onEvent(AppConfigChangedEvent(AppConfig.Key.LOG_REQUEST))
        onEvent(AppConfigChangedEvent(AppConfig.Key.LOG_CACHE))
        onEvent(AppConfigChangedEvent(AppConfig.Key.LOG_ZOOM))
        onEvent(AppConfigChangedEvent(AppConfig.Key.LOG_HUGE_IMAGE))

        onEvent(AppConfigChangedEvent(AppConfig.Key.MOBILE_NETWORK_PAUSE_DOWNLOAD))
        onEvent(AppConfigChangedEvent(AppConfig.Key.GLOBAL_LOW_QUALITY_IMAGE))
        onEvent(AppConfigChangedEvent(AppConfig.Key.GLOBAL_IN_PREFER_QUALITY_OVER_SPEED))
        onEvent(AppConfigChangedEvent(AppConfig.Key.GLOBAL_DISABLE_CACHE_IN_DISK))
        onEvent(AppConfigChangedEvent(AppConfig.Key.GLOBAL_DISABLE_BITMAP_POOL))
        onEvent(AppConfigChangedEvent(AppConfig.Key.GLOBAL_DISABLE_CACHE_IN_MEMORY))

        configuration!!.errorTracker = SampleErrorTracker(context!!)

        configuration!!.uriModelRegistry.add(XpkIconUriModel())
    }

    @Subscribe
    fun onEvent(event: AppConfigChangedEvent) {
        when {
            AppConfig.Key.OUT_LOG_2_SDCARD == event.key -> {
                val proxy = if (AppConfig.getBoolean(context!!, AppConfig.Key.OUT_LOG_2_SDCARD)) SampleLogProxy(context!!) else null
                SLog.setProxy(proxy)
            }
            AppConfig.Key.LOG_LEVEL == event.key -> {
                var levelValue: String? = AppConfig.getString(context!!, AppConfig.Key.LOG_LEVEL)
                if (levelValue == null) {
                    levelValue = ""
                }
                when (levelValue) {
                    "VERBOSE" -> SLog.setLevel(SLog.LEVEL_VERBOSE)
                    "DEBUG" -> SLog.setLevel(SLog.LEVEL_DEBUG)
                    "INFO" -> SLog.setLevel(SLog.LEVEL_INFO)
                    "ERROR" -> SLog.setLevel(SLog.LEVEL_ERROR)
                    "WARNING" -> SLog.setLevel(SLog.LEVEL_WARNING)
                    "NONE" -> SLog.setLevel(SLog.LEVEL_NONE)
                }
            }
            AppConfig.Key.LOG_TIME == event.key -> if (AppConfig.getBoolean(context!!, AppConfig.Key.LOG_TIME)) {
                SLog.openType(SLog.TYPE_TIME)
            } else {
                SLog.closeType(SLog.TYPE_TIME)
            }
            AppConfig.Key.LOG_REQUEST == event.key -> if (AppConfig.getBoolean(context!!, AppConfig.Key.LOG_REQUEST)) {
                SLog.openType(SLog.TYPE_FLOW)
            } else {
                SLog.closeType(SLog.TYPE_FLOW)
            }
            AppConfig.Key.LOG_CACHE == event.key -> if (AppConfig.getBoolean(context!!, AppConfig.Key.LOG_CACHE)) {
                SLog.openType(SLog.TYPE_CACHE)
            } else {
                SLog.closeType(SLog.TYPE_CACHE)
            }
            AppConfig.Key.LOG_ZOOM == event.key -> if (AppConfig.getBoolean(context!!, AppConfig.Key.LOG_ZOOM)) {
                SLog.openType(SLog.TYPE_ZOOM)
            } else {
                SLog.closeType(SLog.TYPE_ZOOM)
            }
            AppConfig.Key.LOG_HUGE_IMAGE == event.key -> if (AppConfig.getBoolean(context!!, AppConfig.Key.LOG_HUGE_IMAGE)) {
                SLog.openType(SLog.TYPE_HUGE_IMAGE)
            } else {
                SLog.closeType(SLog.TYPE_HUGE_IMAGE)
            }
            AppConfig.Key.MOBILE_NETWORK_PAUSE_DOWNLOAD == event.key -> configuration!!.isMobileDataPauseDownloadEnabled = AppConfig.getBoolean(context!!, AppConfig.Key.MOBILE_NETWORK_PAUSE_DOWNLOAD)
            AppConfig.Key.GLOBAL_LOW_QUALITY_IMAGE == event.key -> configuration!!.isLowQualityImageEnabled = AppConfig.getBoolean(context!!, AppConfig.Key.GLOBAL_LOW_QUALITY_IMAGE)
            AppConfig.Key.GLOBAL_IN_PREFER_QUALITY_OVER_SPEED == event.key -> configuration!!.isInPreferQualityOverSpeedEnabled = AppConfig.getBoolean(context!!, AppConfig.Key.GLOBAL_IN_PREFER_QUALITY_OVER_SPEED)
            AppConfig.Key.GLOBAL_DISABLE_CACHE_IN_DISK == event.key -> configuration!!.diskCache.isDisabled = AppConfig.getBoolean(context!!, AppConfig.Key.GLOBAL_DISABLE_CACHE_IN_DISK)
            AppConfig.Key.GLOBAL_DISABLE_BITMAP_POOL == event.key -> configuration!!.bitmapPool.isDisabled = AppConfig.getBoolean(context!!, AppConfig.Key.GLOBAL_DISABLE_BITMAP_POOL)
            AppConfig.Key.GLOBAL_DISABLE_CACHE_IN_MEMORY == event.key -> configuration!!.memoryCache.isDisabled = AppConfig.getBoolean(context!!, AppConfig.Key.GLOBAL_DISABLE_CACHE_IN_MEMORY)
        }
    }
}
