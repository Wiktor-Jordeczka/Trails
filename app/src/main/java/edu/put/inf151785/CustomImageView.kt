package edu.put.inf151785

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import kotlin.math.atan2
import kotlin.math.sqrt


// Zmodyfikowany imageView, który można rozciągać, obracać i przesuwać
class CustomImageView : androidx.appcompat.widget.AppCompatImageView, OnTouchListener {
    var myMatrix = Matrix()
    private var savedMatrix = Matrix()
    private var savedMatrix2 = Matrix()
    private var mode = NONE
    private lateinit var lastEvent: FloatArray
    var d = 0f

    // Remember some things for zooming
    var start = PointF()
    private var mid = PointF()
    private var oldDist = 1f

    constructor(context: Context) : super(context) {
        setOnTouchListener(this)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setOnTouchListener(this)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        view.scaleType = ScaleType.MATRIX
        val scale: Float
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(myMatrix)
                start[event.x] = event.y
                mode = DRAG
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(myMatrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
                lastEvent = FloatArray(4)
                lastEvent[0] = event.getX(0)
                lastEvent[1] = event.getX(1)
                lastEvent[2] = event.getY(0)
                lastEvent[3] = event.getY(1)
                d = rotation(event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                // ...
                myMatrix.set(savedMatrix)
                myMatrix.postTranslate(event.x - start.x, event.y - start.y)
            } else if (mode == ZOOM && event.pointerCount == 2) {
                val newDist = spacing(event)
                myMatrix.set(savedMatrix)
                if (newDist > 10f) {
                    scale = newDist / oldDist
                    myMatrix.postScale(scale, scale, mid.x, mid.y)
                }
                val newRot = rotation(event)
                val r = newRot - d
                myMatrix.postRotate(
                    r, (view.measuredWidth / 2).toFloat(),
                    (view.measuredHeight / 2).toFloat()
                )
            }
        }
        // Perform the transformation
        fixing()
        view.imageMatrix = savedMatrix2
        return true // indicate event was handled
    }

    private fun fixing() {
        val value = FloatArray(9)
        myMatrix.getValues(value)
        val savedValue = FloatArray(9)
        savedMatrix2.getValues(savedValue)
        val width = this.width
        val height = this.height
        val d = this.drawable ?: return
        val imageWidth = d.intrinsicWidth
        val imageHeight = d.intrinsicHeight
        val scaleWidth = (imageWidth * value[0]).toInt()
        val scaleHeight = (imageHeight * value[4]).toInt()

    // don't let the image go outside
        if (value[2] > width - 1) value[2] =
            (width - 10).toFloat() else if (value[5] > height - 1) value[5] =
            (height - 10).toFloat() else if (value[2] < -(scaleWidth - 1)) value[2] =
            -(scaleWidth - 10).toFloat() else if (value[5] < -(scaleHeight - 1)) value[5] =
            -(scaleHeight - 10).toFloat()
        myMatrix.setValues(value)
        savedMatrix2.set(myMatrix)
    }

    private fun rotation(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1)).toDouble()
        val deltaY = (event.getY(0) - event.getY(1)).toDouble()
        val radians = atan2(deltaY, deltaX)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    companion object {
        // We can be in one of these 3 states
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
    }
}