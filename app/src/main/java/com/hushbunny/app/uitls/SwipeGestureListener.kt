package com.hushbunny.app.uitls

import android.view.MotionEvent
import android.view.View
import timber.log.Timber
import kotlin.math.abs

class SwipeGestureListener(private val gestureInterface: SwipeGestureInterface) : View.OnTouchListener {

    private var downX: Float? = null
    private var downY: Float? = null
    private var upX: Float? = null
    private var upY: Float? = null

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                v?.performClick()
                return true
            }
            MotionEvent.ACTION_UP -> {
                v?.performClick()
                upX = event.x
                upY = event.y
                val deltaX = downX?.minus(upX ?: 0f) ?: 0f
                val deltaY = downY?.minus(upY ?: 0f) ?: 0f

                Timber.d("CheckValue: $deltaX $deltaY")

                if(abs(deltaY) > abs(deltaX)) {
                    if (deltaY < 0) {
                        gestureInterface.onTopToBottomSwipe(v);
                        return true;
                    }
                    if (deltaY > 0) {
                        gestureInterface.onBottomToTopSwipe(v);
                        return true;
                    }

                } else {
                    if (deltaX < 0) {
                        gestureInterface.onLeftToRightSwipe(v);
                        return true;
                    }
                    if (deltaX > 0) {
                        gestureInterface.onRightToLeftSwipe(v);
                        return true;
                    }
                }
            }
        }
        return false
    }
}

interface SwipeGestureInterface {
    fun onRightToLeftSwipe(v: View?)
    fun onLeftToRightSwipe(v: View?)
    fun onTopToBottomSwipe(v: View?)
    fun onBottomToTopSwipe(v: View?)
}
