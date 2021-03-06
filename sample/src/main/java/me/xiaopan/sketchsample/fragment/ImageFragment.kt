package me.xiaopan.sketchsample.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.format.Formatter
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import me.xiaopan.sketch.Sketch
import me.xiaopan.sketch.datasource.DataSource
import me.xiaopan.sketch.display.FadeInImageDisplayer
import me.xiaopan.sketch.drawable.ImageAttrs
import me.xiaopan.sketch.drawable.SketchDrawable
import me.xiaopan.sketch.drawable.SketchGifDrawable
import me.xiaopan.sketch.drawable.SketchRefBitmap
import me.xiaopan.sketch.request.*
import me.xiaopan.sketch.state.MemoryCacheStateImage
import me.xiaopan.sketch.uri.FileUriModel
import me.xiaopan.sketch.uri.GetDataSourceException
import me.xiaopan.sketch.uri.UriModel
import me.xiaopan.sketch.util.SketchUtils
import me.xiaopan.sketch.viewfun.huge.HugeImageViewer
import me.xiaopan.sketch.viewfun.zoom.ImageZoomer
import me.xiaopan.sketchsample.BaseFragment
import me.xiaopan.sketchsample.BindContentView
import me.xiaopan.sketchsample.R
import me.xiaopan.sketchsample.activity.PageBackgApplyCallback
import me.xiaopan.sketchsample.bean.Image
import me.xiaopan.sketchsample.event.AppConfigChangedEvent
import me.xiaopan.sketchsample.util.AppConfig
import me.xiaopan.sketchsample.util.ApplyWallpaperAsyncTask
import me.xiaopan.sketchsample.util.SaveImageAsyncTask
import me.xiaopan.sketchsample.widget.HintView
import me.xiaopan.sketchsample.widget.MappingView
import me.xiaopan.sketchsample.widget.SampleImageView
import me.xiaopan.ssvt.bindView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.IOException
import java.util.*

@BindContentView(R.layout.fragment_image)
class ImageFragment : BaseFragment() {

    val imageView: SampleImageView by bindView(R.id.image_imageFragment_image)
    val mappingView: MappingView by bindView(R.id.mapping_imageFragment)
    val hintView: HintView by bindView(R.id.hint_imageFragment_hint)

    lateinit var image: Image
    private var loadingImageOptionsKey: String? = null
    private var showTools: Boolean = false

    private var finalShowImageUrl: String? = null

    private val setWindowBackground = SetWindowBackground()
    private val gifPlayFollowPageVisible = GifPlayFollowPageVisible()
    private val showImageHelper = ShowImageHelper()
    private val imageZoomHelper = ImageZoomHelper()
    private val mappingHelper = MappingHelper()
    private val hugeImageHelper = HugeImageHelper()
    private val clickHelper = ClickHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowBackground.onCreate(activity)

        arguments?.let {
            image = it.getParcelable<Image>(PARAM_REQUIRED_STRING_IMAGE_URI)
            loadingImageOptionsKey = it.getString(PARAM_REQUIRED_STRING_LOADING_IMAGE_OPTIONS_KEY)
            showTools = it.getBoolean(PARAM_REQUIRED_BOOLEAN_SHOW_TOOLS)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showHighDefinitionImage = AppConfig.getBoolean(context, AppConfig.Key.SHOW_UNSPLASH_RAW_IMAGE)
        finalShowImageUrl = if (showHighDefinitionImage && !TextUtils.isEmpty(image.rawQualityUrl)) image.rawQualityUrl else image.normalQualityUrl

        imageZoomHelper.onViewCreated()
        hugeImageHelper.onViewCreated()
        mappingHelper.onViewCreated()
        clickHelper.onViewCreated()
        showImageHelper.onViewCreated()

        EventBus.getDefault().register(this)
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    public override fun onUserVisibleChanged(isVisibleToUser: Boolean) {
        hugeImageHelper.onUserVisibleChanged()
        setWindowBackground.onUserVisibleChanged()
        gifPlayFollowPageVisible.onUserVisibleChanged()
    }

    @Suppress("unused")
    @Subscribe
    fun onEvent(event: AppConfigChangedEvent) {
        if (AppConfig.Key.SUPPORT_ZOOM == event.key) {
            imageZoomHelper.onConfigChanged()
            mappingHelper.onViewCreated()
        } else if (AppConfig.Key.READ_MODE == event.key) {
            imageZoomHelper.onReadModeConfigChanged()
        } else if (AppConfig.Key.SUPPORT_HUGE_IMAGE == event.key) {
            hugeImageHelper.onConfigChanged()
            mappingHelper.onViewCreated()
        }
    }

    class PlayImageEvent

    private inner class ShowImageHelper : DisplayListener, DownloadProgressListener {
        fun onViewCreated() {
            imageView.displayListener = this
            imageView.downloadProgressListener = this

            initOptions()
            imageView.displayImage(finalShowImageUrl!!)
        }

        private fun initOptions() {
            imageView.page = SampleImageView.Page.DETAIL

            val options = imageView.options

            // 允许播放GIF
            options.isDecodeGifImage = true

            // 有占位图选项信息的话就使用内存缓存占位图但不使用任何显示器，否则就是用渐入显示器
            if (!TextUtils.isEmpty(loadingImageOptionsKey)) {
                val uriModel = UriModel.match(activity, finalShowImageUrl!!)
                var cachedRefBitmap: SketchRefBitmap? = null
                var memoryCacheKey: String? = null
                if (uriModel != null) {
                    memoryCacheKey = SketchUtils.makeRequestKey(image.normalQualityUrl ?: "", uriModel, loadingImageOptionsKey!!)
                    cachedRefBitmap = Sketch.with(activity).configuration.memoryCache.get(memoryCacheKey)
                }
                if (cachedRefBitmap != null) {
                    options.loadingImage = MemoryCacheStateImage(memoryCacheKey, null)
                } else {
                    options.displayer = FadeInImageDisplayer()
                }
            } else {
                options.displayer = FadeInImageDisplayer()
            }
        }

        override fun onStarted() {
            hintView.loading(null)
        }

        override fun onCompleted(drawable: Drawable, imageFrom: ImageFrom, imageAttrs: ImageAttrs) {
            hintView.hidden()

            setWindowBackground.onDisplayCompleted()
            gifPlayFollowPageVisible.onDisplayCompleted()
        }

        override fun onError(cause: ErrorCause) {
            hintView.hint(R.drawable.ic_error, "Image display failed", "Again", View.OnClickListener { imageView.displayImage(finalShowImageUrl!!) })
        }

        override fun onCanceled(cause: CancelCause) {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (cause) {
                CancelCause.PAUSE_DOWNLOAD -> hintView.hint(R.drawable.ic_error, "Pause to download new image for saving traffic", "I do not care", View.OnClickListener {
                    val requestLevel = imageView.options.requestLevel
                    imageView.options.requestLevel = RequestLevel.NET
                    imageView.displayImage(finalShowImageUrl!!)
                    imageView.options.requestLevel = requestLevel
                })
                CancelCause.PAUSE_LOAD -> hintView.hint(R.drawable.ic_error, "Paused to load new image", "Forced to load", View.OnClickListener {
                    val requestLevel = imageView.options.requestLevel
                    imageView.options.requestLevel = RequestLevel.NET
                    imageView.displayImage(finalShowImageUrl!!)
                    imageView.options.requestLevel = requestLevel
                })
            }
        }

        override fun onUpdateDownloadProgress(totalLength: Int, completedLength: Int) {
            hintView.setProgress(totalLength, completedLength)
        }
    }

    private inner class SetWindowBackground {
        private var pageBackgApplyCallback: PageBackgApplyCallback? = null

        fun onCreate(activity: Activity) {
            if (activity is PageBackgApplyCallback) {
                setWindowBackground.pageBackgApplyCallback = activity
            }
        }

        fun onUserVisibleChanged() {
            if (pageBackgApplyCallback != null && isVisibleToUser) {
                pageBackgApplyCallback!!.onApplyBackground(finalShowImageUrl)
            }
        }

        fun onDisplayCompleted() {
            onUserVisibleChanged()
        }
    }

    private inner class GifPlayFollowPageVisible {
        fun onUserVisibleChanged() {
            val drawable = imageView.drawable
            val lastDrawable = SketchUtils.getLastDrawable(drawable)
            if (lastDrawable != null && lastDrawable is SketchGifDrawable) {
                (lastDrawable as SketchGifDrawable).followPageVisible(isVisibleToUser, false)
            }
        }

        fun onDisplayCompleted() {
            val drawable = imageView.drawable
            val lastDrawable = SketchUtils.getLastDrawable(drawable)
            if (lastDrawable != null && lastDrawable is SketchGifDrawable) {
                (lastDrawable as SketchGifDrawable).followPageVisible(isVisibleToUser, true)
            }
        }
    }

    private inner class ImageZoomHelper {
        fun onViewCreated() {
            imageView.isZoomEnabled = AppConfig.getBoolean(imageView.context, AppConfig.Key.SUPPORT_ZOOM)
            onReadModeConfigChanged()
        }

        fun onConfigChanged() {
            onViewCreated()
        }

        fun onReadModeConfigChanged() {
            if (imageView.isZoomEnabled) {
                val readMode = AppConfig.getBoolean(activity, AppConfig.Key.READ_MODE)
                imageView.imageZoomer!!.isReadMode = readMode
            }
        }
    }

    private inner class HugeImageHelper {
        fun onViewCreated() {
            imageView.isHugeImageEnabled = AppConfig.getBoolean(imageView.context, AppConfig.Key.SUPPORT_HUGE_IMAGE)

            // 初始化超大图查看器的暂停状态，这一步很重要
            if (AppConfig.getBoolean(activity, AppConfig.Key.PAGE_VISIBLE_TO_USER_DECODE_HUGE_IMAGE) && imageView.isHugeImageEnabled) {
                imageView.hugeImageViewer!!.setPause(!isVisibleToUser)
            }
        }

        fun onConfigChanged() {
            onViewCreated()
        }

        fun onUserVisibleChanged() {
            // 不可见的时候暂停超大图查看器，节省内存
            if (AppConfig.getBoolean(activity, AppConfig.Key.PAGE_VISIBLE_TO_USER_DECODE_HUGE_IMAGE)) {
                if (imageView.isHugeImageEnabled) {
                    imageView.hugeImageViewer!!.setPause(!isVisibleToUser)
                }
            } else {
                if (imageView.isHugeImageEnabled
                        && isVisibleToUser && imageView.hugeImageViewer!!.isPaused) {
                    imageView.hugeImageViewer!!.setPause(false)
                }
            }
        }
    }

    private inner class MappingHelper {
        fun onViewCreated() {
            // MappingView跟随碎片变化刷新碎片区域
            if (imageView.isHugeImageEnabled) {
                imageView.hugeImageViewer!!.onTileChangedListener = HugeImageViewer.OnTileChangedListener { hugeImageViewer -> mappingView.tileChanged(hugeImageViewer) }
            }

            // MappingView跟随Matrix变化刷新显示区域
            if (imageView.isZoomEnabled) {
                imageView.imageZoomer!!.addOnMatrixChangeListener(object : ImageZoomer.OnMatrixChangeListener {
                    internal var visibleRect = Rect()

                    override fun onMatrixChanged(imageZoomer: ImageZoomer) {
                        imageZoomer.getVisibleRect(visibleRect)
                        mappingView.update(imageZoomer.drawableSize, visibleRect)
                    }
                })
            }

            // 点击MappingView定位到指定位置
            mappingView.setOnSingleClickListener(object : MappingView.OnSingleClickListener {
                override fun onSingleClick(x: Float, y: Float): Boolean {
                    val drawable = imageView.drawable ?: return false

                    if (drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
                        return false
                    }

                    if (mappingView.width == 0 || mappingView.height == 0) {
                        return false
                    }

                    val widthScale = drawable.intrinsicWidth.toFloat() / mappingView.width
                    val heightScale = drawable.intrinsicHeight.toFloat() / mappingView.height
                    val realX = x * widthScale
                    val realY = y * heightScale

                    val showLocationAnimation = AppConfig.getBoolean(imageView.context, AppConfig.Key.LOCATION_ANIMATE)
                    location(realX, realY, showLocationAnimation)
                    return true
                }
            })

            mappingView.options.displayer = FadeInImageDisplayer()
            mappingView.options.setMaxSize(600, 600)
            mappingView.displayImage(finalShowImageUrl!!)

            mappingView.visibility = if (showTools) View.VISIBLE else View.GONE
        }

        fun location(x: Float, y: Float, animate: Boolean): Boolean {
            imageView.imageZoomer?.location(x, y, animate)
            return true
        }
    }

    private inner class ClickHelper {

        fun onViewCreated() {
            // 将单击事件传递给上层 Activity
            imageView.onClickListener = View.OnClickListener { v ->
                val parentFragment = parentFragment
                if (parentFragment != null && parentFragment is ImageZoomer.OnViewTapListener) {
                    (parentFragment as ImageZoomer.OnViewTapListener).onViewTap(v, 0f, 0f)
                }
            }

            // 长按显示菜单
            imageView.setOnLongClickListener {
                showMenu()
                true
            }
        }

        fun showMenu() {
            val menuItemList = LinkedList<MenuItem>()

            menuItemList.add(MenuItem(
                    "Image Info",
                    DialogInterface.OnClickListener { _, _ -> imageView.showInfo(activity) }
            ))
            menuItemList.add(MenuItem(
                    "Zoom/Rotate/Huge Image",
                    DialogInterface.OnClickListener { _, _ -> showZoomMenu() }
            ))
            menuItemList.add(MenuItem(
                    String.format("Toggle ScaleType (%s)", imageView.imageZoomer?.scaleType ?: imageView.scaleType),
                    DialogInterface.OnClickListener { _, _ -> showScaleTypeMenu() }
            ))
            menuItemList.add(MenuItem(
                    "Auto Play",
                    DialogInterface.OnClickListener { _, _ -> play() }
            ))
            menuItemList.add(MenuItem(
                    "Set as wallpaper",
                    DialogInterface.OnClickListener { _, _ -> setWallpaper() }
            ))
            menuItemList.add(MenuItem(
                    "Share Image",
                    DialogInterface.OnClickListener { _, _ -> share() }
            ))
            menuItemList.add(MenuItem(
                    "Save Image",
                    DialogInterface.OnClickListener { _, _ -> save() }
            ))

            val items = arrayOfNulls<String>(menuItemList.size)
            var w = 0
            val size = menuItemList.size
            while (w < size) {
                items[w] = menuItemList[w].title
                w++
            }

            val itemClickListener = DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                menuItemList[which].clickListener?.onClick(dialog, which)
            }

            AlertDialog.Builder(activity)
                    .setItems(items, itemClickListener)
                    .show()
        }

        fun showZoomMenu() {
            val menuItemList = LinkedList<MenuItem>()

            val imageZoomer = imageView.imageZoomer
            if (imageZoomer != null) {
                val zoomInfoBuilder = StringBuilder()
                val zoomScale = SketchUtils.formatFloat(imageZoomer.zoomScale, 2)
                val visibleRect = Rect()
                imageZoomer.getVisibleRect(visibleRect)
                val visibleRectString = visibleRect.toShortString()
                zoomInfoBuilder.append("Zoom: ").append(zoomScale).append(" / ").append(visibleRectString)
                menuItemList.add(MenuItem(zoomInfoBuilder.toString(), null))
            } else {
                menuItemList.add(MenuItem("Zoom (Disabled)", null))
            }

            val hugeImageViewer = imageView.hugeImageViewer
            if (hugeImageViewer != null) {
                val hugeImageInfoBuilder = StringBuilder()
                if (hugeImageViewer.isReady) {
                    hugeImageInfoBuilder.append("Huge image tiles：")
                            .append(hugeImageViewer.tiles)
                            .append("/")
                            .append(hugeImageViewer.tileList.size)
                            .append("/")
                            .append(Formatter.formatFileSize(context, hugeImageViewer.tilesAllocationByteCount))

                    hugeImageInfoBuilder.append("\n")
                    hugeImageInfoBuilder.append("Huge image decode area：").append(hugeImageViewer.decodeRect.toShortString())

                    hugeImageInfoBuilder.append("\n")
                    hugeImageInfoBuilder.append("Huge image decode src area：").append(hugeImageViewer.decodeSrcRect.toShortString())
                } else if (hugeImageViewer.isInitializing) {
                    hugeImageInfoBuilder.append("\n")
                    hugeImageInfoBuilder.append("Huge image initializing...")
                } else {
                    hugeImageInfoBuilder.append("Huge image (No need)")
                }
                menuItemList.add(MenuItem(hugeImageInfoBuilder.toString(), null))
            } else {
                menuItemList.add(MenuItem("Huge image (Disabled)", null))
            }

            if (hugeImageViewer != null) {
                if (hugeImageViewer.isReady || hugeImageViewer.isInitializing) {
                    menuItemList.add(MenuItem(
                            if (hugeImageViewer.isShowTileRect) "Hide block boundary" else "Show block boundary",
                            DialogInterface.OnClickListener { _, _ -> toggleShowTileEdge() }))
                } else {
                    menuItemList.add(MenuItem("Block boundary (No need huge image)", null))
                }
            } else {
                menuItemList.add(MenuItem("Block boundary (Huge image disabled)", null))
            }

            if (imageZoomer != null) {
                menuItemList.add(MenuItem(
                        if (imageZoomer.isReadMode) "Close read mode" else "Open read mode",
                        DialogInterface.OnClickListener { _, _ -> toggleReadMode() }))
            } else {
                menuItemList.add(MenuItem("Read mode (Zoom disabled)", null))
            }

            if (imageZoomer != null) {
                menuItemList.add(MenuItem(
                        String.format("Clockwise rotation 90°（%d）", imageZoomer.rotateDegrees),
                        DialogInterface.OnClickListener { _, _ -> rotate() }))
            } else {
                menuItemList.add(MenuItem("Clockwise rotation 90° (Zoom disabled)", null))
            }

            val items = arrayOfNulls<String>(menuItemList.size)
            var w = 0
            val size = menuItemList.size
            while (w < size) {
                items[w] = menuItemList[w].title
                w++
            }

            val itemClickListener = DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                menuItemList[which].clickListener?.onClick(dialog, which)
            }

            AlertDialog.Builder(activity)
                    .setItems(items, itemClickListener)
                    .show()
        }

        fun showScaleTypeMenu() {
            val builder = AlertDialog.Builder(activity)

            builder.setTitle("Toggle ScaleType")

            val items = arrayOfNulls<String>(7)
            items[0] = "CENTER"
            items[1] = "CENTER_CROP"
            items[2] = "CENTER_INSIDE"
            items[3] = "FIT_START"
            items[4] = "FIT_CENTER"
            items[5] = "FIT_END"
            items[6] = "FIT_XY"

            builder.setItems(items) { dialog, which ->
                dialog.dismiss()

                when (which) {
                    0 -> imageView.scaleType = ImageView.ScaleType.CENTER
                    1 -> imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    2 -> imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    3 -> imageView.scaleType = ImageView.ScaleType.FIT_START
                    4 -> imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    5 -> imageView.scaleType = ImageView.ScaleType.FIT_END
                    6 -> imageView.scaleType = ImageView.ScaleType.FIT_XY
                }
            }

            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        fun toggleShowTileEdge() {
            imageView.hugeImageViewer?.let {
                it.isShowTileRect = !it.isShowTileRect
            }
        }

        fun toggleReadMode() {
            imageView.imageZoomer?.let {
                it.isReadMode = !it.isReadMode
            }
        }

        fun rotate() {
            imageView.imageZoomer?.let {
                if (!it.rotateBy(90)) {
                    Toast.makeText(context, "The rotation angle must be a multiple of 90", Toast.LENGTH_LONG).show()
                }
            }
        }

        fun getImageFile(imageUri: String?): File? {
            if (TextUtils.isEmpty(imageUri)) {
                return null
            }

            val uriModel = UriModel.match(context, imageUri!!)
            if (uriModel == null) {
                Toast.makeText(activity, "Unknown format uri: " + imageUri, Toast.LENGTH_LONG).show()
                return null
            }

            val dataSource: DataSource
            try {
                dataSource = uriModel.getDataSource(context, imageUri, null)
            } catch (e: GetDataSourceException) {
                e.printStackTrace()
                Toast.makeText(activity, "The Image is not ready yet", Toast.LENGTH_LONG).show()
                return null
            }

            try {
                return dataSource.getFile(context.externalCacheDir, null)
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }

        }

        fun share() {
            val drawable = imageView.drawable
            val imageUri = if (drawable != null && drawable is SketchDrawable) (drawable as SketchDrawable).uri else null
            if (TextUtils.isEmpty(imageUri)) {
                Toast.makeText(activity, "Please wait later", Toast.LENGTH_LONG).show()
                return
            }

            val imageFile = getImageFile(imageUri)
            if (imageFile == null) {
                Toast.makeText(activity, "The Image is not ready yet", Toast.LENGTH_LONG).show()
                return
            }

            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile))
            intent.type = "image/" + parseFileType(imageFile.name)!!

            val infoList = activity.packageManager.queryIntentActivities(intent, 0)
            if (infoList == null || infoList.isEmpty()) {
                Toast.makeText(activity, "There is no APP on your device to share the picture", Toast.LENGTH_LONG).show()
                return
            }

            startActivity(intent)
        }

        fun setWallpaper() {
            val drawable = imageView.drawable
            val imageUri = if (drawable != null && drawable is SketchDrawable) (drawable as SketchDrawable).uri else null
            if (TextUtils.isEmpty(imageUri)) {
                Toast.makeText(activity, "Please wait later", Toast.LENGTH_LONG).show()
                return
            }

            val imageFile = getImageFile(imageUri)
            if (imageFile == null) {
                Toast.makeText(activity, "The Image is not ready yet", Toast.LENGTH_LONG).show()
                return
            }

            ApplyWallpaperAsyncTask(activity, imageFile).execute(0)
        }

        fun play() {
            EventBus.getDefault().post(PlayImageEvent())
        }

        fun save() {
            val drawable = imageView.drawable
            val imageUri = if (drawable != null && drawable is SketchDrawable) (drawable as SketchDrawable).uri else null
            if (TextUtils.isEmpty(imageUri)) {
                Toast.makeText(activity, "Please wait later", Toast.LENGTH_LONG).show()
                return
            }

            val uriModel = UriModel.match(context, imageUri!!)
            if (uriModel == null) {
                Toast.makeText(activity, "Unknown format uri: " + imageUri, Toast.LENGTH_LONG).show()
                return
            }

            if (uriModel is FileUriModel) {
                Toast.makeText(activity, "This image is the local no need to save", Toast.LENGTH_LONG).show()
                return
            }

            val dataSource: DataSource
            try {
                dataSource = uriModel.getDataSource(context, imageUri, null)
            } catch (e: GetDataSourceException) {
                e.printStackTrace()
                Toast.makeText(activity, "The Image is not ready yet", Toast.LENGTH_LONG).show()
                return
            }

            SaveImageAsyncTask(activity, dataSource, imageUri).execute("")
        }

        fun parseFileType(fileName: String): String? {
            val lastIndexOf = fileName.lastIndexOf(".")
            if (lastIndexOf < 0) {
                return null
            }
            val fileType = fileName.substring(lastIndexOf + 1)
            if ("" == fileType.trim { it <= ' ' }) {
                return null
            }
            return fileType
        }

        private inner class MenuItem(val title: String, var clickListener: DialogInterface.OnClickListener?)
    }

    companion object {
        val PARAM_REQUIRED_STRING_IMAGE_URI = "PARAM_REQUIRED_STRING_IMAGE_URI"
        val PARAM_REQUIRED_STRING_LOADING_IMAGE_OPTIONS_KEY = "PARAM_REQUIRED_STRING_LOADING_IMAGE_OPTIONS_KEY"
        val PARAM_REQUIRED_BOOLEAN_SHOW_TOOLS = "PARAM_REQUIRED_BOOLEAN_SHOW_TOOLS"

        fun build(image: Image, loadingImageOptionsId: String?, showTools: Boolean): ImageFragment {
            val bundle = Bundle()
            bundle.putParcelable(PARAM_REQUIRED_STRING_IMAGE_URI, image)
            bundle.putString(PARAM_REQUIRED_STRING_LOADING_IMAGE_OPTIONS_KEY, loadingImageOptionsId)
            bundle.putBoolean(PARAM_REQUIRED_BOOLEAN_SHOW_TOOLS, showTools)
            val fragment = ImageFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}