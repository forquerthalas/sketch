/*
 * Copyright (C) 2017 Peng fei Pan <sky@xiaopan.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.sketchsample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.support.annotation.IntDef
import android.util.SparseArray
import me.xiaopan.sketch.display.TransitionImageDisplayer
import me.xiaopan.sketch.process.GaussianBlurImageProcessor
import me.xiaopan.sketch.request.DisplayOptions
import me.xiaopan.sketch.request.DownloadOptions
import me.xiaopan.sketch.request.LoadOptions
import me.xiaopan.sketch.request.ShapeSize
import me.xiaopan.sketch.shaper.CircleImageShaper
import me.xiaopan.sketch.shaper.RoundRectImageShaper
import me.xiaopan.sketch.state.DrawableStateImage
import me.xiaopan.sketch.state.OldStateImage
import me.xiaopan.sketch.util.SketchUtils
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

object ImageOptions {
    /**
     * 通用矩形
     */
    const val RECT = 101

    /**
     * 带描边的圆形
     */
    const val CIRCULAR_STROKE = 102

    /**
     * 窗口背景
     */
    const val WINDOW_BACKGROUND = 103

    /**
     * 圆角矩形
     */
    const val ROUND_RECT = 104

    /**
     * 充满列表
     */
    const val LIST_FULL = 105

    @JvmStatic
    private val OPTIONS_HOLDER_SPARSE_ARRAY = SparseArray<OptionsHolder>()

    init {
        val transitionImageDisplayer = TransitionImageDisplayer()

        OPTIONS_HOLDER_SPARSE_ARRAY.append(ImageOptions.RECT, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                return DisplayOptions()
                        .setLoadingImage(R.drawable.image_loading)
                        .setErrorImage(R.drawable.image_error)
                        .setPauseDownloadImage(R.drawable.image_pause_download)
                        .setDisplayer(transitionImageDisplayer)
                        .setShapeSize(ShapeSize.byViewFixedSize())
            }
        })

        OPTIONS_HOLDER_SPARSE_ARRAY.append(ImageOptions.CIRCULAR_STROKE, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                return DisplayOptions()
                        .setLoadingImage(R.drawable.image_loading)
                        .setErrorImage(R.drawable.image_error)
                        .setPauseDownloadImage(R.drawable.image_pause_download)
                        .setDisplayer(transitionImageDisplayer)
                        .setShaper(CircleImageShaper().setStroke(Color.WHITE, SketchUtils.dp2px(context, 1)))
                        .setShapeSize(ShapeSize.byViewFixedSize())
            }
        })

        OPTIONS_HOLDER_SPARSE_ARRAY.append(ImageOptions.WINDOW_BACKGROUND, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                return DisplayOptions()
                        .setLoadingImage(OldStateImage(DrawableStateImage(R.drawable.shape_window_background)))
                        .setProcessor(GaussianBlurImageProcessor.makeLayerColor(Color.parseColor("#66000000")))
                        .setCacheProcessedImageInDisk(true)
                        .setBitmapConfig(Bitmap.Config.ARGB_8888)   // 效果比较重要
                        .setShapeSize(ShapeSize.byViewFixedSize())
                        .setMaxSize(context.resources.displayMetrics.widthPixels / 4,
                                context.resources.displayMetrics.heightPixels / 4)
                        .setDisplayer(TransitionImageDisplayer(true))
            }
        })

        OPTIONS_HOLDER_SPARSE_ARRAY.append(ImageOptions.ROUND_RECT, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                return DisplayOptions()
                        .setLoadingImage(R.drawable.image_loading)
                        .setErrorImage(R.drawable.image_error)
                        .setPauseDownloadImage(R.drawable.image_pause_download)
                        .setShaper(RoundRectImageShaper(SketchUtils.dp2px(context, 6).toFloat()))
                        .setDisplayer(transitionImageDisplayer)
                        .setShapeSize(ShapeSize.byViewFixedSize())
            }
        })

        OPTIONS_HOLDER_SPARSE_ARRAY.append(ImageOptions.LIST_FULL, object : OptionsHolder() {
            override fun onCreateOptions(context: Context): DownloadOptions {
                val displayMetrics = context.resources.displayMetrics
                return DisplayOptions()
                        .setLoadingImage(R.drawable.image_loading)
                        .setErrorImage(R.drawable.image_error)
                        .setPauseDownloadImage(R.drawable.image_pause_download)
                        .setMaxSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
                        .setDisplayer(transitionImageDisplayer)
            }
        })
    }

    fun getDisplayOptions(context: Context, @Type optionsId: Int): DisplayOptions {
        return OPTIONS_HOLDER_SPARSE_ARRAY.get(optionsId).getOptions(context) as DisplayOptions
    }

    fun getLoadOptions(context: Context, @Type optionsId: Int): LoadOptions {
        return OPTIONS_HOLDER_SPARSE_ARRAY.get(optionsId).getOptions(context) as LoadOptions
    }

    fun getDownloadOptions(context: Context, @Type optionsId: Int): DownloadOptions {
        return OPTIONS_HOLDER_SPARSE_ARRAY.get(optionsId).getOptions(context)
    }

    private abstract class OptionsHolder {
        private var options: DownloadOptions? = null

        fun getOptions(context: Context): DownloadOptions {
            if (options == null) {
                synchronized(this) {
                    if (options == null) {
                        options = onCreateOptions(context)
                    }
                }
            }
            return options!!
        }

        protected abstract fun onCreateOptions(context: Context): DownloadOptions
    }

    @IntDef(RECT.toLong(), CIRCULAR_STROKE.toLong(), WINDOW_BACKGROUND.toLong(), ROUND_RECT.toLong(), LIST_FULL.toLong())
    @Retention(RetentionPolicy.SOURCE)
    annotation class Type
}
