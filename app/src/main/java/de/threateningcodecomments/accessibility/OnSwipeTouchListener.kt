package de.threateningcodecomments.accessibility

import android.content.Context
import android.view.View.OnTouchListener
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View
import de.threateningcodecomments.accessibility.OnSwipeTouchListener.GestureListener
import java.lang.Exception
import kotlin.math.abs

class OnSwipeTouchListener(context: Context?, val action: SwipeBlock) : OnTouchListener {
    private val gestureDetector: GestureDetector

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val isClick = event.action == MotionEvent.ACTION_BUTTON_RELEASE

        if(isClick)
            v.performClick()

        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            return super.onDown(e)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_DISTANCE_THRESHOLD && abs(velocityX) > Companion.SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            action(RC.Directions.RIGHT)
                        } else {
                            action(RC.Directions.LEFT)
                        }
                        result = true
                    }
                } else if (abs(diffY) > SWIPE_DISTANCE_THRESHOLD && abs(velocityY) > Companion.SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        action(RC.Directions.DOWN)
                    } else {
                        action(RC.Directions.UP)
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    companion object {
        private const val SWIPE_DISTANCE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }
}

typealias SwipeBlock = (direction: Int) -> Unit