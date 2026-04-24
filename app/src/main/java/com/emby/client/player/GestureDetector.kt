package com.emby.client.player

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class GestureDetector(context: Context, private val listener: GestureListener) : GestureDetector.SimpleOnGestureListener() {

    interface GestureListener {
        fun onHorizontalSwipe(distance: Float)
        fun onVerticalSwipeLeft(distance: Float)
        fun onVerticalSwipeRight(distance: Float)
    }

    private val detector = GestureDetector(context, this)

    fun attachToView(view: View) {
        view.setOnTouchListener { _, event ->
            detector.onTouchEvent(event)
        }
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null) return false

        val deltaX = e2.x - e1.x
        val deltaY = e2.y - e1.y

        val absDeltaX = Math.abs(deltaX)
        val absDeltaY = Math.abs(deltaY)

        if (absDeltaX > absDeltaY) {
            // Horizontal swipe (progress control)
            listener.onHorizontalSwipe(deltaX)
        } else {
            // Vertical swipe (volume/brightness control)
            val screenWidth = e1.view?.width ?: 1080
            val touchX = e1.x
            
            if (touchX < screenWidth / 2) {
                // Left side: brightness
                listener.onVerticalSwipeLeft(deltaY)
            } else {
                // Right side: volume
                listener.onVerticalSwipeRight(deltaY)
            }
        }

        return true
    }
}
