package com.hushbunny.app.uitls

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.hushbunny.app.R

class PinchToZoomView(context: Context, attrs: AttributeSet) :
    AppCompatImageView(
        context,
        attrs
    ),
    View.OnTouchListener,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {
    private var scaleDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null
    private var mMatrix: Matrix? = null
    private var matrixValues: FloatArray? = null
    private var mode = NONE

    private var saveScale = ONE
    private var minScale = ONE
    private var maxScale = FOUR

    private var originalWidth = ZERO
    private var originalHeight = ZERO
    private var viewWidth = NONE
    private var viewHeight = NONE
    private var lastPoint = PointF()
    private var startPoint = PointF()

    var isZoomEnabled = false

    init {
        super.setClickable(true)
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        mMatrix = Matrix()
        matrixValues = FloatArray(SIZE_NINE)
        imageMatrix = mMatrix
        scaleType = ScaleType.MATRIX
        gestureDetector = GestureDetector(context, this)
        setOnTouchListener(this)
        val attributes = getContext().obtainStyledAttributes(attrs, R.styleable.PinchToZoomView)
        try {
            if (!isInEditMode) {
                isZoomEnabled =
                    attributes.getBoolean(R.styleable.PinchToZoomView_zoom_enabled, true)
            }
        } finally {
            // release the TypedArray so that it can be reused.
            attributes.recycle()
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val prevScale = saveScale
            saveScale *= mScaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / prevScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / prevScale
            }
            if (originalWidth * saveScale <= viewWidth || originalHeight * saveScale <= viewHeight
            ) {
                mMatrix!!.postScale(
                    mScaleFactor, mScaleFactor, viewWidth / 2.toFloat(),
                    viewHeight / 2.toFloat()
                )
            } else {
                mMatrix!!.postScale(
                    mScaleFactor, mScaleFactor,
                    detector.focusX, detector.focusY
                )
            }
            fixTranslation()
            return true
        }
    }

    private fun fitToScreen() {
        saveScale = ONE
        val scale: Float
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == NONE || drawable.intrinsicHeight == NONE) return
        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight
        val scaleX = viewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = viewHeight.toFloat() / imageHeight.toFloat()
        scale = scaleX.coerceAtMost(scaleY)
        mMatrix!!.setScale(scale, scale)

        var redundantYSpace = (viewHeight.toFloat() - scale * imageHeight.toFloat())
        var redundantXSpace = (viewWidth.toFloat() - scale * imageWidth.toFloat())
        redundantYSpace /= ZOOM.toFloat()
        redundantXSpace /= ZOOM.toFloat()
        mMatrix!!.postTranslate(redundantXSpace, redundantYSpace)
        originalWidth = viewWidth - ZOOM * redundantXSpace
        originalHeight = viewHeight - ZOOM * redundantYSpace
        imageMatrix = mMatrix
    }

    fun fixTranslation() {
        mMatrix!!.getValues(matrixValues)
        val transX =
            matrixValues!![Matrix.MTRANS_X]
        val transY =
            matrixValues!![Matrix.MTRANS_Y]
        val fixTransX = getFixTranslation(transX, viewWidth.toFloat(), originalWidth * saveScale)
        val fixTransY = getFixTranslation(transY, viewHeight.toFloat(), originalHeight * saveScale)
        if (fixTransX != ZERO || fixTransY != ZERO) mMatrix!!.postTranslate(fixTransX, fixTransY)
    }

    private fun getFixTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) {
            minTrans = ZERO
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = ZERO
        }
        if (trans < minTrans) {
            return -trans + minTrans
        }
        if (trans > maxTrans) {
            return -trans + maxTrans
        }
        return ZERO
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            ZERO
        } else delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (saveScale == ONE) {
            fitToScreen()
        }
    }

    private fun stopInterceptEvent() {
        parent.requestDisallowInterceptTouchEvent(true)
    }

    private fun startInterceptEvent() {
        parent.requestDisallowInterceptTouchEvent(false)
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        if (isZoomEnabled) {
            scaleDetector!!.onTouchEvent(event)
        }
        gestureDetector!!.onTouchEvent(event)
        val currentPoint = PointF(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastPoint.set(currentPoint)
                startPoint.set(lastPoint)
                mode = DRAG
            }
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                val dx = currentPoint.x - lastPoint.x
                val dy = currentPoint.y - lastPoint.y
                val fixTransX = getFixDragTrans(dx, viewWidth.toFloat(), originalWidth * saveScale)
                val fixTransY =
                    getFixDragTrans(dy, viewHeight.toFloat(), originalHeight * saveScale)
                mMatrix!!.postTranslate(fixTransX, fixTransY)
                if (saveScale == 1.0.toFloat()) {
                    startInterceptEvent()
                } else {
                    stopInterceptEvent()
                }
                fixTranslation()
                lastPoint[currentPoint.x] = currentPoint.y
            }
            MotionEvent.ACTION_POINTER_UP -> mode = NONE
        }
        imageMatrix = mMatrix
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        fitToScreen()
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {}

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return true
    }

    override fun onLongPress(e: MotionEvent?) {}

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return true
    }

    companion object {
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
        const val ONE = 1f
        const val ZERO = 0f
        const val FOUR = 4f
        const val SIZE_NINE = 9
    }
}
