package me.xiaopan.sketchsample.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import me.xiaopan.sketch.display.TransitionImageDisplayer
import me.xiaopan.sketch.request.ShapeSize
import me.xiaopan.sketchsample.AssetImage
import me.xiaopan.sketchsample.BaseFragment
import me.xiaopan.sketchsample.BindContentView
import me.xiaopan.sketchsample.R
import me.xiaopan.sketchsample.widget.SampleImageView
import me.xiaopan.ssvt.bindView

@BindContentView(R.layout.fragment_resize)
class ShapeSizeImageShaperTestFragment : BaseFragment() {
    val imageView: SampleImageView by bindView(R.id.image_resizeFragment)
    val widthSeekBar: SeekBar by bindView(R.id.seekBar_resizeFragment_width)
    val widthProgressTextView: TextView by bindView(R.id.text_resizeFragment_width)
    val heightSeekBar: SeekBar by bindView(R.id.seekBar_resizeFragment_height)
    val heightProgressTextView: TextView by bindView(R.id.text_resizeFragment_height)
    val fixStartButton: View by bindView(R.id.button_resizeFragment_fixStart)
    val fixCenterButton: View by bindView(R.id.button_resizeFragment_fixCenter)
    val fixEndButton: View by bindView(R.id.button_resizeFragment_fixEnd)
    val fixXYButton: View by bindView(R.id.button_resizeFragment_fixXY)
    val centerButton: View by bindView(R.id.button_resizeFragment_center)
    val centerCropButton: View by bindView(R.id.button_resizeFragment_centerCrop)
    val centerInsideButton: View by bindView(R.id.button_resizeFragment_centerInside)
    val matrixButton: View by bindView(R.id.button_resizeFragment_matrix)

    private var widthProgress = 50
    private var heightProgress = 50
    private var scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER
    private var currentCheckedButton: View? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                widthProgress = widthSeekBar.progress
                apply(currentCheckedButton)
            }
        })
        widthSeekBar.progress = widthProgress

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
                heightProgress = heightSeekBar.progress
                apply(currentCheckedButton)
            }
        })
        heightSeekBar.progress = heightProgress

        fixStartButton.tag = ImageView.ScaleType.FIT_START
        fixCenterButton.tag = ImageView.ScaleType.FIT_CENTER
        fixEndButton.tag = ImageView.ScaleType.FIT_END
        fixXYButton.tag = ImageView.ScaleType.FIT_XY
        centerButton.tag = ImageView.ScaleType.CENTER
        centerCropButton.tag = ImageView.ScaleType.CENTER_CROP
        centerInsideButton.tag = ImageView.ScaleType.CENTER_INSIDE
        matrixButton.tag = ImageView.ScaleType.MATRIX

        val buttonOnClickListener = View.OnClickListener { v ->
            scaleType = v.tag as ImageView.ScaleType
            apply(v)
        }
        fixStartButton.setOnClickListener(buttonOnClickListener)
        fixCenterButton.setOnClickListener(buttonOnClickListener)
        fixEndButton.setOnClickListener(buttonOnClickListener)
        fixXYButton.setOnClickListener(buttonOnClickListener)
        centerButton.setOnClickListener(buttonOnClickListener)
        centerCropButton.setOnClickListener(buttonOnClickListener)
        centerInsideButton.setOnClickListener(buttonOnClickListener)
        matrixButton.setOnClickListener(buttonOnClickListener)

        if (currentCheckedButton == null) {
            currentCheckedButton = fixCenterButton
        }
        apply(currentCheckedButton)
    }

    private fun apply(button: View?) {
        val width = (widthProgress / 100f * 1000).toInt()
        val height = (heightProgress / 100f * 1000).toInt()

        imageView.options.shapeSize = ShapeSize(width, height, scaleType)
        imageView.displayImage(AssetImage.MEI_NV)

        currentCheckedButton?.isEnabled = true
        button?.isEnabled = false
        currentCheckedButton = button
    }
}
