package me.xiaopan.sketchsample.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import me.xiaopan.sketch.display.TransitionImageDisplayer
import me.xiaopan.sketch.process.MaskImageProcessor
import me.xiaopan.sketch.process.RotateImageProcessor
import me.xiaopan.sketch.process.RoundRectImageProcessor
import me.xiaopan.sketchsample.AssetImage
import me.xiaopan.sketchsample.BaseFragment
import me.xiaopan.sketchsample.BindContentView
import me.xiaopan.sketchsample.R
import me.xiaopan.sketchsample.widget.SampleImageView
import me.xiaopan.ssvt.bindView

@BindContentView(R.layout.fragment_wrapped)
class WrappedImageProcessorTestFragment : BaseFragment() {
    val imageView: SampleImageView by bindView(R.id.image_wrappedFragment)
    val widthSeekBar: SeekBar by bindView(R.id.seekBar_wrappedFragment_width)
    val widthProgressTextView: TextView by bindView(R.id.text_wrappedFragment_width)
    val heightSeekBar: SeekBar by bindView(R.id.seekBar_wrappedFragment_height)
    val heightProgressTextView: TextView by bindView(R.id.text_wrappedFragment_height)
    val rotateButton: View by bindView(R.id.button_wrappedFragment)

    private var roundRectRadiusProgress = 30
    private var maskAlphaProgress = 45
    private var rotateProgress = 45

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 缩小图片，处理速度更快，更少的内存消耗
        val metrics = resources.displayMetrics
        imageView.options.setMaxSize(metrics.widthPixels / 2, metrics.heightPixels / 2)

        imageView.options.displayer = TransitionImageDisplayer()

        widthSeekBar.max = 100
        widthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progress < 20) {
                    widthSeekBar.progress = 20
                    return
                }

                val width = (widthSeekBar.progress / 100f * 1000).toInt()
                widthProgressTextView.text = String.format("%d/%d", width, 1000)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                roundRectRadiusProgress = widthSeekBar.progress
                apply()
            }
        })
        widthSeekBar.progress = roundRectRadiusProgress

        heightSeekBar.max = 100
        heightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progress < 20) {
                    heightSeekBar.progress = 20
                    return
                }
                val height = (heightSeekBar.progress / 100f * 1000).toInt()
                heightProgressTextView.text = String.format("%d/%d", height, 1000)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                maskAlphaProgress = heightSeekBar.progress
                apply()
            }
        })
        heightSeekBar.progress = maskAlphaProgress

        rotateButton.setOnClickListener {
            rotateProgress += 45
            apply()
        }

        apply()
    }

    private fun apply() {
        val roundRectImageProcessor = RoundRectImageProcessor(roundRectRadiusProgress.toFloat())
        val rotateImageProcessor = RotateImageProcessor(rotateProgress, roundRectImageProcessor)

        val alpha = (maskAlphaProgress.toFloat() / 100 * 255).toInt()
        val maskColor = Color.argb(alpha, 0, 0, 0)

        imageView.options.processor = MaskImageProcessor(maskColor, rotateImageProcessor)
        imageView.displayImage(AssetImage.MEI_NV)
    }
}
