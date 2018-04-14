package com.jiubao.customView

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Scroller
import com.jiubao.testintent.R
import java.util.ArrayList

/**
 * 功能描述:
 * 作者： Administrator 时间： 2018/4/13.
 * 版本:
 */
class PieChart @JvmOverloads constructor(context: Context,
                                         attr:AttributeSet? = null)
    : ViewGroup(context,attr){

    companion object {
        @JvmField
        val TEXTPOS_LEFT = 0

        @JvmField
        val TEXTPOS_RIGHT = 1

        @JvmField
        val FLING_VELOCITY_DOWNSCALE = 4

        @JvmField
        val AUTOCENTER_ANIM_DURATION = 250


        @JvmStatic
        fun vectorToScalarScroll(dx:Float,dy:Float,x:Float,y:Float):Float{
            val l = Math.sqrt((dx * dx + dy * dy) as Double) as Float
            val crossX = -y
            val crossY = x
            val dot = (crossX * dx + crossY * dy)
            val sign = Math.signum(dot)
            return l * sign
        }
    }



    val data = ArrayList<Item>()
    var total = 0.0f
    var pieBounds = RectF()
    var piePaint : Paint? = null
    var textPaint:Paint? = null
    var shadowPaint:Paint? = null
    var showText = false
        set(value) {
            field = value
            invalidate()
        }
    var textX = 0.0f
    var textY = 0.0f
        set(value) {
            field = value
            invalidate()
        }
    var textWidth = 0.0f
        set(value) {
            field = value
            invalidate()
        }

    var textHeight = 0.0f
        set(value) {
            field = value
            invalidate()
        }
    var textPos = TEXTPOS_LEFT
        set(value) {
            if(value != TEXTPOS_LEFT && value != TEXTPOS_RIGHT){
                throw IllegalArgumentException("TextPos must be one of TEXTPOS_LEFT or TEXTPOS_RIGHT")
            }
            field = value
            invalidate()
        }
    var highlightStrength = 1.15f
        set(value) {
            if(value < 0.0f){
                throw IllegalArgumentException("highlight strength cannot be negative")
            }
            field = value
            invalidate()
        }
    var pointerRadius = 2.0f
        set(value) {
            field = value
            invalidate()
        }
    var pointerX = 0.0f
    var pointerY = 0.0f
    var pieRotation = 0
        set(value) {
            var value2 = (value % 360 + 360) % 360
            field = value2
            pieView?.rotateTo(value2.toFloat())
            calcCurrentItem()
        }
    var mCurrentItemChangedListener:OnCurrentItemChangedListener? = null
    var textColor = 0
    var pieView:PieView? = null
    var scroller:Scroller? = null
    var valueAnimator:ValueAnimator? = null
    var detector:GestureDetector? = null
    var pointView:PointerView? = null
    var currentItemAngle = 0
    var currentItem = 0
        private set

    var autoCenterInSlice = false
    var objectAnimator:ObjectAnimator? = null
    var shadowBounds:RectF = RectF()

    fun setCurrentItem2(value:Int){
        setCurrentItem2(value,true)
    }

    private fun setCurrentItem2(value:Int,scrollIntoView: Boolean){
        currentItem = currentItem
        mCurrentItemChangedListener?.OnCurrentItemChanged(this, currentItem)
        if (scrollIntoView) {
            centerOnCurrentItem()
        }
        invalidate()
    }

    fun addItem(label: String,value: Float,color: Int):Int{
        val it = Item()
        it.label = label
        it.value = value
        it.color = color
        it.highLight = Color.argb(0xff,
                Math.min((highlightStrength * Color.red(color).toFloat()).toInt(),0xff),
                Math.min((highlightStrength * Color.green(color).toFloat()).toInt(),0xff),
                Math.min((highlightStrength * Color.blue(color).toFloat()).toInt(),0xff)
        )
        total += value
        data.add(it)
        onDataChanged()
        return data.size - 1
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val result = detector?.onTouchEvent(event)
        if(result != null && !result){
            if(event?.action == MotionEvent.ACTION_UP){
                stopScrolling()
                return true
            }
        }
        return false
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        // Do nothing. Do not call the superclass method--that would start a layout pass
        // on this view's children. PieChart lays out its children in onSizeChanged().
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawOval(shadowBounds,shadowPaint)
        if(showText){
            canvas?.drawText(data[currentItem].label,textX,textY,textPaint)
        }
        // If the API level is less than 11, we can't rely on the view animation system to
        // do the scrolling animation. Need to tick it here and call postInvalidate() until the scrolling is done.
        if(Build.VERSION.SDK_INT < 11){
            tickScrollAnimation()
            if(scroller != null && !scroller!!.isFinished){
                postInvalidate()
            }
        }
    }

    override fun getSuggestedMinimumHeight(): Int {
        return textWidth.toInt()
    }

    override fun getSuggestedMinimumWidth(): Int {
        return (textWidth * 2).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = Math.max(minW,MeasureSpec.getSize(widthMeasureSpec))

        val minH = (w - textWidth.toInt()) + paddingBottom + paddingTop
        val h = Math.min(MeasureSpec.getSize(heightMeasureSpec),minH)
        setMeasuredDimension(w,h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        var xpad = (paddingLeft + paddingRight).toFloat()
        var ypad = (paddingTop + paddingBottom).toFloat()

        if(showText){
            xpad += textWidth
        }

        var ww = w - xpad
        var hh = h - ypad

        var diameter = Math.min(ww,hh)
        pieBounds = RectF(0.0f,0.0f,diameter,diameter)
        pieBounds.offsetTo(paddingLeft.toFloat(),paddingTop.toFloat())

        pointerY = textY - (textHeight / 2.0f).toInt()
        var pointerOffset = pieBounds.centerY() - pointerY
        if(textPos == TEXTPOS_LEFT){
            textPaint?.textAlign = Paint.Align.RIGHT
            if(showText) {
                pieBounds.offset(textWidth, 0.0f)
            }
            textX = pieBounds.left
            if(pointerOffset < 0){
                pointerOffset = 0 - pointerOffset
                currentItemAngle = 225
            }else{
                currentItemAngle = 135
            }
            pointerX = pieBounds.centerX() - pointerOffset
        }else{
            textPaint?.textAlign = Paint.Align.LEFT
            textX = pieBounds.right
            if(pointerOffset < 0){
                pointerOffset = 0 - pointerOffset
                currentItemAngle = 315
            }else{
                currentItemAngle = 45
            }
            pointerX = pieBounds.centerX() + pointerOffset
        }

        shadowBounds = RectF(pieBounds.left + 10,
                            pieBounds.bottom + 10,
                            pieBounds.right - 10,
                            pieBounds.bottom + 20)
        pieView?.layout(pieBounds.left.toInt(),
                        pieBounds.top.toInt(),
                        pieBounds.right.toInt(),
                        pieBounds.bottom.toInt())
        pieView?.setPivot(pieBounds.width() / 2,pieBounds.height() / 2)
        pointView?.layout(0,0,w,h)
        onDataChanged()
    }

    fun calcCurrentItem(){
        val pointerAngle = (currentItemAngle + 360 + pieRotation) % 360
        for(i in data.indices){
            val item = data[i]
            if(item.startAngle <= pointerAngle && pointerAngle <= item.endAngle){
                if(i != currentItem){
                    setCurrentItem2(i,false)
                }
                break
            }
        }
    }

    fun onDataChanged(){
        var currentAngle = 0
        for (it in data){
            it.startAngle = currentAngle
            it.endAngle = (currentAngle.toFloat() + it.value * 360.0f / total).toInt()
            currentAngle = it.endAngle

            // Recalculate the gradient shaders. There are
            // three values in this gradient, even though only
            // two are necessary, in order to work around
            // a bug in certain versions of the graphics engine
            // that expects at least three values if the
            // positions array is non-null.
            //
            it.shader = SweepGradient(pieBounds.width() / 2.0f,
                                    pieBounds.height() / 2.0f,
                                    arrayOf(it.highLight,it.highLight,it.color,it.color).toIntArray(),
                                    arrayOf(0.0f,(360 - it.endAngle).toFloat(),
                                            (360 - it.startAngle).toFloat(),1.0f).toFloatArray())
        }
        calcCurrentItem()
        onScrollFinished()
    }

    init {
        if(attr != null){
            val a = context.theme.obtainStyledAttributes(attr,
                    R.styleable.PieChart,0,0)
            showText = a.getBoolean(R.styleable.PieChart_showText, false)
            textY = a.getDimension(R.styleable.PieChart_labelY, 0.0f);
            textWidth = a.getDimension(R.styleable.PieChart_labelWidth, 0.0f);
            textHeight = a.getDimension(R.styleable.PieChart_labelHeight, 0.0f);
            textPos = a.getInteger(R.styleable.PieChart_labelPosition, 0);
            textColor = a.getColor(R.styleable.PieChart_labelColor, 0xff000000.toInt());
            highlightStrength = a.getFloat(R.styleable.PieChart_highlightStrength, 1.0f);
            pieRotation = a.getInt(R.styleable.PieChart_pieRotation, 0);
            pointerRadius = a.getDimension(R.styleable.PieChart_pointerRadius, 2.0f);
            autoCenterInSlice = a.getBoolean(R.styleable.PieChart_autoCenterPointerInSlice, false);
            a.recycle()
        }

        init()
    }

    private fun init() {
        setLayerToSW(this)
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
        }
        if(textHeight.toInt() == 0){
            textHeight = textPaint!!.textSize
        }else{
            textPaint!!.textSize = textHeight
        }
        piePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            textSize = textHeight
        }

        shadowPaint = Paint(0).apply {
            color = Color.parseColor("0xff101010")
            maskFilter = BlurMaskFilter(8.0f,BlurMaskFilter.Blur.NORMAL)
        }

        pieView = PieView(context)
        addView(pieView)
        pieView!!.rotateTo(pieRotation.toFloat())

        pointView = PointerView(context)
        addView(pointView)

        if(Build.VERSION.SDK_INT >= 11){
            objectAnimator = ObjectAnimator.ofInt(this,"PieRotation",0).apply {
                addListener(object : Animator.AnimatorListener{
                    override fun onAnimationStart(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        pieView!!.decelerate()
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationRepeat(p0: Animator?) {
                    }
                })
            }
        }
        if(Build.VERSION.SDK_INT < 11){
            scroller = Scroller(context)
        }else{
            scroller = Scroller(context,null,false)
        }

        if(Build.VERSION.SDK_INT >= 11){
            valueAnimator = ValueAnimator.ofFloat(0.0f,1.0f).apply {
                addUpdateListener {
                    tickScrollAnimation()
                }
            }
        }
        detector = GestureDetector(context,GestureListener())
        detector?.setIsLongpressEnabled(false)



    }

    private fun setLayerToSW(v:View){

    }

    private fun setLayerToHW(v:View){

    }


    fun onScrollFinished(){

    }

    fun tickScrollAnimation(){

    }



    fun stopScrolling(){

    }



    fun centerOnCurrentItem(){

    }


    data class Item(var label:String = "",
                    var value:Float = 0.0f,
                    var color:Int = 0,
                    var startAngle:Int = 0,
                    var endAngle:Int = 0,
                    var highLight:Int = 0,
                    var shader:Shader? = null)


    inner class GestureListener : GestureDetector.SimpleOnGestureListener(){
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return super.onDown(e)
        }
    }

    interface OnCurrentItemChangedListener{
        fun OnCurrentItemChanged(pieChart: PieChart,currentItem:Int)
    }

    inner class PieView @JvmOverloads constructor(context: Context,
                                                    attr: AttributeSet? = null)
        : View(context,attr){
        var rotation = 0
        var transForm = Matrix()
        var pivot:PointF = PointF()

        fun accelerate(){

        }

        fun decelerate(){

        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
        }

        fun rotateTo(pieRotation:Float){

        }

        fun setPivot(x:Float,y:Float){

        }
    }

    inner class PointerView @JvmOverloads constructor(context: Context
                                                              ,attr: AttributeSet? = null)
        :View(context,attr){
        override fun onDraw(canvas: Canvas?) {
            canvas?.drawLine(textX,pointerY,pointerX,pointerY,textPaint)
            canvas?.drawCircle(pointerX,pointerY,pointerRadius,textPaint)
        }
    }
}





















