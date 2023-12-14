package com.example.viewloadstatuslibs.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.transition.TransitionManager
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.setPadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator


val Float.dp
    get()= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,this, Resources.getSystem().displayMetrics)

val Int.dp
    get() = this.toFloat().dp

@SuppressLint("ViewConstructor")
class ViewLoadStatus(context: Context) : LinearLayout(context) {
    enum class VIEW_STATUS {
        LOADING, ERROR, EMPTY, FINISHED
    }
    enum class VIEW_TYPE {
        TEXT_VIEW,DEFAULT
    }
    private val TAG = "ViewLoadStatus"
    private var mHeight = 0f
    private var mWidth = 0f
    private var mDefaultHeight = 100.dp
    private var currentView: View? = null
    private val mViewPadding = 5.dp
    private val mBigStatusMinWidth=150.dp
    private val mBigStatusMinHeight=100.dp
    private var viewStatus = VIEW_STATUS.FINISHED
    private var viewType= VIEW_TYPE.DEFAULT
    private var message = "加载错误，点击重试"
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        if (Utils.isDarkModeEnabled(context)) {
            color = Color.parseColor("#20ffffff")
        } else {
            color = Color.parseColor("#20000000")
        }
    }
    private var onErrorRetryClickListener: ((View) -> Unit)? = null
    private var onEmptyRetryClickListener: ((View) -> Unit)? = null

    fun setOnErrorRetryClickListener(click: (v: View) -> Unit) {
        this.onErrorRetryClickListener = click
    }

    fun setOnEmptyRetryClickListener(click: (v: View) -> Unit) {
        this.onEmptyRetryClickListener = click
    }

    init {
        gravity = Gravity.CENTER
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    fun showViewIsLoading(view: View) {
        if (viewStatus == VIEW_STATUS.LOADING) return
        val viewGroup = view.parent as ViewGroup
        viewGroup.removeView(this)
        viewStatus = VIEW_STATUS.LOADING

        getChildSize(view)

        setViewAlignAddViewStatus(viewGroup, view)
    }

    fun showViewIsError(view: View, msg: String = "加载失败，点击重试") {
        if (viewStatus == VIEW_STATUS.ERROR) return
        this.message = msg
        val viewGroup = view.parent as ViewGroup
        viewGroup.removeView(this)
        viewStatus = VIEW_STATUS.ERROR


        getChildSize(view)

        setViewAlignAddViewStatus(viewGroup, view)
    }

    fun showViewEmpty(view: View, msg: String = "什么也没有，点击重试") {
        if (viewStatus == VIEW_STATUS.EMPTY) return
        this.message = msg
        val viewGroup = view.parent as ViewGroup
        viewGroup.removeView(this)
        viewStatus = VIEW_STATUS.EMPTY

        getChildSize(view)
        setViewAlignAddViewStatus(viewGroup, view)
    }

    fun finished(view: View) {
        if (viewStatus == VIEW_STATUS.FINISHED) return
        val viewGroup = view.parent as ViewGroup
        viewStatus = VIEW_STATUS.FINISHED
        TransitionManager.beginDelayedTransition(viewGroup)
        view.layoutParams = layoutParams
        view.visibility = View.VISIBLE

        viewGroup.removeView(this)
    }

    fun getCurrentViewStatus()=viewStatus


    private fun setViewAlignAddViewStatus(viewGroup: ViewGroup, view: View) {
        TransitionManager.beginDelayedTransition(viewGroup)
        viewGroup.removeView(this)
        if (viewGroup is ConstraintLayout) {
            view.visibility = View.INVISIBLE
            val contentViewLayoutParams = view.layoutParams as ConstraintLayout.LayoutParams
            layoutParams = contentViewLayoutParams

            viewGroup.addView(this)
        } else if (viewGroup is RelativeLayout) {
            view.visibility = View.INVISIBLE
            val contentViewLayoutParams = view.layoutParams as RelativeLayout.LayoutParams
            layoutParams = contentViewLayoutParams

            viewGroup.addView(this)
        } else if (viewGroup is LinearLayout) {
            view.visibility = View.GONE
            val contentViewLayoutParams = view.layoutParams as LinearLayout.LayoutParams
            layoutParams = contentViewLayoutParams

            val indexOf = viewGroup.children.indexOf(view)

            viewGroup.addView(this, indexOf)
        } else {
            view.visibility = View.GONE
            val contentViewLayoutParams = view.layoutParams as ViewGroup.LayoutParams
            layoutParams = contentViewLayoutParams

            viewGroup.addView(this)
        }
    }

    private fun getChildSize(view: View) {
        currentView = view
        viewType=when(view){
            is TextView-> VIEW_TYPE.TEXT_VIEW
            else -> VIEW_TYPE.DEFAULT
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 获取子视图的真实宽高
                mWidth = view.width.toFloat()
                mHeight = view.height.toFloat()
                if(mHeight.toInt()==0){
                    mHeight=mDefaultHeight
                }
                if(viewType== VIEW_TYPE.TEXT_VIEW){
                    mHeight=-1f
                }
                Log.i(TAG, "onGlobalLayout: ${view.width} ${view.height}")
                refreshViews()
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun refreshViews() {
        when (viewStatus) {
            VIEW_STATUS.LOADING -> {
                addLoadingView()
            }

            VIEW_STATUS.ERROR -> {
                addErrorView()
            }

            VIEW_STATUS.EMPTY -> {
                addEmptyView()
            }

            VIEW_STATUS.FINISHED -> {}
        }
        currentView?.apply {
            setViewAlignAddViewStatus(parent as ViewGroup, this)
        }
    }

    private fun addLoadingView() {
        removeAllViews()
        addView(LinearLayout(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, mHeight.toInt())
            gravity = Gravity.CENTER
            addView(CircularProgressIndicator(context).apply {
                isIndeterminate = true
                if(viewType== VIEW_TYPE.TEXT_VIEW){
                    indicatorSize= 15.dp.toInt()
                    trackThickness=2.dp.toInt()
                }
                val newLayoutParams = LayoutParams(80.dp.toInt(), 80.dp.toInt())
                layoutParams = newLayoutParams
            })
        })
    }

    private fun addErrorView() {
        removeAllViews()
        addView(LinearLayout(context).apply {
            layoutParams = LayoutParams(WRAP_CONTENT, mHeight.toInt())
            setPadding(mViewPadding.toInt())
            gravity = Gravity.CENTER
            orientation = VERTICAL
            val errorTextView = TextView(context).apply {
                layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                text = message
                gravity=Gravity.CENTER
            }
            addView(errorTextView)
            if(mHeight>mBigStatusMinHeight&&mWidth>mBigStatusMinWidth){
                val errorButton = MaterialButton(context).apply {
                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#d3712a"))
                    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                    text = "重试"
                    setOnClickListener {
                        Log.i(TAG, "addErrorView: $onErrorRetryClickListener $onEmptyRetryClickListener")
                        onErrorRetryClickListener?.invoke(it)
                    }
                }
                addView(errorButton)
            }else{
                setOnClickListener {
                    onErrorRetryClickListener?.invoke(it)
                }
            }
        })
    }

    private fun addEmptyView() {
        removeAllViews()
        addView(LinearLayout(context).apply {
            setPadding(mViewPadding.toInt())
            layoutParams = LayoutParams(WRAP_CONTENT, mHeight.toInt())
            gravity = Gravity.CENTER
            orientation = VERTICAL
            val errorTextView = TextView(context).apply {
                layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                text = message
                gravity=Gravity.CENTER
            }
            addView(errorTextView)
            if(mHeight>mBigStatusMinHeight&&mWidth>mBigStatusMinWidth){
                val emptyButton = MaterialButton(context).apply {
                    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                    text = "再次尝试"
                    setOnClickListener {
                        onEmptyRetryClickListener?.invoke(it)
                    }
                }
                addView(emptyButton)
            }else{
                setOnClickListener {
                    onEmptyRetryClickListener?.invoke(it)
                }
            }
        })
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (viewStatus != VIEW_STATUS.FINISHED) {
            canvas.drawRoundRect(
                0f,
                0f,
                width.toFloat() - 0f,
                height.toFloat() - 0f,
                8.dp,
                8.dp,
                paint
            )
        }
        super.dispatchDraw(canvas)
    }
}