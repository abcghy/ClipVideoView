package com.example.clipbarview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntDef

class ClipBarView : View {

    companion object {
        const val LEFT = 0
        const val RIGHT = 1
        const val MIDDLE = 2
        const val OTHERS = 3

        @IntDef(LEFT, RIGHT, MIDDLE, OTHERS)
        annotation class SlideState
    }

    @ColorInt
    private val frameColor: Int

    private val radius: Float
    private val sliderWidth: Float

    private val strokeWidth: Float

    // 总时间长度
    private var mDuration: Float = 100f
    private var mStart: Float = 0f
    private var mEnd: Float = 100f

    private var tempStart = mStart
    private var tempEnd = mEnd

    private val framePaint: Paint
    private val bitmapPaint: Paint

    @SlideState
    private var state: Int = OTHERS
    private var touchX: Float = 0f
//    private var touchY: Float = 0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        frameColor = context.resources.getColor(R.color.frameColor)
        radius = context.dp2px(4f).toFloat()
        sliderWidth = context.dp2px(15f).toFloat()
        strokeWidth = context.dp2px(4f).toFloat()

        framePaint = Paint()
        framePaint.isAntiAlias = true
        framePaint.color = frameColor

        bitmapPaint = Paint()
        bitmapPaint.isAntiAlias = true
        bitmapPaint.color = Color.WHITE

//        if (isInEditMode) {
//            setAllDuration(10000)
//            mStart = 1000
//            mEnd = 9000
//        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        if (mDuration == 0L) return

        val left: Float = measuredWidth * (mStart.toFloat() / mDuration)
        val right: Float = measuredWidth * (mEnd.toFloat() / mDuration)

        // region 左右两个滑块
        canvas.drawCircle(left + radius, radius, radius, framePaint)
        canvas.drawCircle(left + radius, measuredHeight.toFloat() - radius, radius, framePaint)
        canvas.drawCircle(right - radius, radius, radius, framePaint) // todo
        canvas.drawCircle(right - radius, measuredHeight.toFloat() - radius, radius, framePaint)

        canvas.drawRect(left, radius, left + sliderWidth, measuredHeight - radius, framePaint)
        canvas.drawRect(left + radius, 0f, left + sliderWidth, measuredHeight.toFloat(), framePaint)

        canvas.drawRect(right - sliderWidth, radius, right, measuredHeight - radius, framePaint)
        canvas.drawRect(right - sliderWidth, 0f, right - radius, measuredHeight.toFloat(), framePaint)
        // endregion

        // region 上下两个线
        canvas.drawRect(left + radius, 0f, right - radius, strokeWidth, framePaint)
        canvas.drawRect(left + radius, measuredHeight.toFloat() - strokeWidth, right - radius, measuredHeight.toFloat(), framePaint)
        // endregion

        // region 左右两个箭头
        canvas.drawRect(left, (measuredHeight / 2 - context.dp2px(10f)).toFloat(), left + context.dp2px(10f), (measuredHeight / 2 + context.dp2px(10f)).toFloat(), bitmapPaint)
        canvas.drawRect(right - context.dp2px(10f), (measuredHeight / 2 - context.dp2px(10f)).toFloat(), right, (measuredHeight / 2 + context.dp2px(10f)).toFloat(), bitmapPaint)
        // endregion
    }

//    fun setAllDuration(allDuration: Long) {
//        mDuration = allDuration
//        mEnd = mDuration
//    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionMasked = event.actionMasked
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (isOnLeftSlide(event.x, event.y)) {
                    state = LEFT
                } else if (isOnRightSlide(event.x, event.y)) {
                    state = RIGHT
                } else if (isInMiddle(event.x, event.y)) {
                    state = MIDDLE
                }
                if (canTouch(event.x, event.y)) {
                    touchX = event.x
//                    touchY = event.y
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (state != OTHERS) {
                    val newX = event.x
                    val diffX = newX - touchX
                    touchX = newX
                    if (state == LEFT) {
                        val left: Float = measuredWidth * (tempStart.toFloat() / mDuration)
                        val leftDiff = left + diffX
                        tempStart = (leftDiff / measuredWidth * mDuration)
                        if (tempStart > mEnd) {
                            mStart = mEnd
                        } else {
                            mStart = tempStart
                        }
                    } else if (state == RIGHT) {
                        val right: Float = measuredWidth * (tempEnd.toFloat() / mDuration)
                        val rightDiff = right + diffX
                        tempEnd = (rightDiff / measuredWidth * mDuration)
                        if (tempEnd < mStart) {
                            mEnd = mStart
                        } else {
                            mEnd = tempEnd
                        }
                    } else if (state == MIDDLE) {
                        val left: Float = measuredWidth * (tempStart.toFloat() / mDuration)
                        val right: Float = measuredWidth * (tempEnd.toFloat() / mDuration)
                        val leftDiff = left + diffX
                        val rightDiff = right + diffX
                        tempStart = (leftDiff / measuredWidth * mDuration)
                        tempEnd = (rightDiff / measuredWidth * mDuration)
                        if (tempStart < 0) {
                            mEnd = tempEnd - tempStart
                            mStart = 0f
                        } else if (tempEnd > mDuration) {
                            mStart = mDuration - (tempEnd - tempStart)
                            mEnd = mDuration
                        } else {
                            mStart = tempStart
                            mEnd = tempEnd
                        }
                    }
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                tempStart = mStart
                tempEnd = mEnd
            }
            MotionEvent.ACTION_CANCEL -> {
                tempStart = mStart
                tempEnd = mEnd
            }
        }
        return super.onTouchEvent(event)
    }

    fun isOnLeftSlide(x: Float, y: Float): Boolean {
        val left: Float = measuredWidth * (mStart.toFloat() / mDuration)

        val leftSlideRect = RectF(left, 0f, left + sliderWidth, measuredHeight.toFloat())
        return leftSlideRect.contains(x, y)
    }

    fun isOnRightSlide(x: Float, y: Float): Boolean {
        val right: Float = measuredWidth * (mEnd.toFloat() / mDuration)
        val rightSlideRect = RectF(right - sliderWidth, 0f, right, measuredHeight.toFloat())
        return rightSlideRect.contains(x, y)
    }

    fun isInMiddle(x: Float, y: Float): Boolean {
        val left: Float = measuredWidth * (mStart.toFloat() / mDuration)
        val right: Float = measuredWidth * (mEnd.toFloat() / mDuration)

        val centerRect = RectF(left + sliderWidth, 0f, right - sliderWidth, measuredHeight.toFloat())
        return centerRect.contains(x, y)
    }

    fun canTouch(x: Float, y: Float): Boolean {
        return isOnLeftSlide(x, y) or isOnRightSlide(x, y) or isInMiddle(x, y)
    }

    fun Context.dp2px(dp: Float): Int = (resources.displayMetrics.density * dp + 0.5f).toInt()
}