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

// 扩展属性，将像素值转换为设备无关像素（dp）
val Float.dp
    get()= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,this, Resources.getSystem().displayMetrics)

val Int.dp
    get() = this.toFloat().dp

@SuppressLint("ViewConstructor")
// 自定义视图类 ViewLoadStatus
class ViewLoadStatus(context: Context) : LinearLayout(context) {
    //View状态枚举
    enum class VIEW_STATUS {
        LOADING, ERROR, EMPTY, FINISHED
    }
    //单独适配TextView
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
    private var mViewStatus = VIEW_STATUS.FINISHED
    private var mViewType= VIEW_TYPE.DEFAULT
    private var mMessage = "加载错误，点击重试"
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (Utils.isDarkModeEnabled(context)) {
            Color.parseColor("#20ffffff")
        } else {
            Color.parseColor("#20000000")
        }
    }
    // 设置加载错误时的点击重试监听器
    private var onErrorRetryClickListener: ((View) -> Unit)? = null
    // 设置空视图时的点击重试监听器
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

    // 设置显示的消息文本
    fun setMessage(msg:String){
        this.mMessage=msg
        refreshViews()
    }

    fun showViewIsLoading(view: View) {
        // 检查当前视图的状态是否为正在加载，如果是的话直接返回，不进行任何操作
        if (mViewStatus == VIEW_STATUS.LOADING) return
        // 检查传入的视图的可见性，如果视图的可见性为View.GONE或View.INVISIBLE，则直接返回，不进行任何操作
        if (view.visibility == (View.GONE or View.INVISIBLE)) return
        // 获取传入视图的父容器（view.parent）作为ViewGroup
        val viewGroup = view.parent as ViewGroup
        // 从父容器中移除当前的加载状态视图（this）
        viewGroup.removeView(this)
        // 将视图的状态设置为正在加载
        mViewStatus = VIEW_STATUS.LOADING
        // 通过调用getChildSize(view)方法来获取传入视图的大小
        getChildSize(view)
        // 通过调用setViewAlignAddViewStatus(viewGroup, view)方法将加载状态视图添加到传入视图的父容器中，
        // 并保持相对于传入视图居中对齐的位置
        setViewAlignAddViewStatus(viewGroup, view)
    }

    fun showViewIsError(view: View, msg: String = "加载失败，点击重试") {
        // 检查当前视图的状态是否为加载错误，如果是的话直接返回，不进行任何操作
        if (mViewStatus == VIEW_STATUS.ERROR) return
        // 检查传入的视图的可见性，如果视图的可见性为View.GONE或View.INVISIBLE，则直接返回，不进行任何操作
        if (view.visibility == (View.GONE or View.INVISIBLE)) return
        // 更新错误消息
        this.mMessage = msg
        // 获取传入视图的父容器（view.parent）作为ViewGroup
        val viewGroup = view.parent as ViewGroup
        // 从父容器中移除当前的加载状态视图（this）
        viewGroup.removeView(this)
        // 将视图的状态设置为加载错误
        mViewStatus = VIEW_STATUS.ERROR
        // 通过调用getChildSize(view)方法来获取传入视图的大小
        getChildSize(view)
        // 通过调用setViewAlignAddViewStatus(viewGroup, view)方法将加载状态视图添加到传入视图的父容器中，
        // 并保持相对于传入视图居中对齐的位置
        setViewAlignAddViewStatus(viewGroup, view)
    }

    fun setViewLoadStatus(viewStatus: VIEW_STATUS, view: View) {
        // 根据传入的视图状态进行不同的处理
        when (viewStatus) {
            VIEW_STATUS.LOADING -> {
                // 如果视图状态为正在加载，则调用showViewIsLoading函数显示加载中的视图
                showViewIsLoading(view)
            }
            VIEW_STATUS.ERROR -> {
                // 如果视图状态为加载错误，则调用showViewIsError函数显示加载错误的视图
                showViewIsError(view)
            }
            VIEW_STATUS.EMPTY -> {
                // 如果视图状态为空，则调用showViewEmpty函数显示空视图
                showViewEmpty(view)
            }
            VIEW_STATUS.FINISHED -> {
                // 如果视图状态为加载完成，则调用finished函数进行相应处理
                finished(view)
            }
        }
    }


    fun showViewEmpty(view: View, msg: String = "什么也没有，点击重试") {
        // 检查当前视图的状态是否为空，如果是的话直接返回，不进行任何操作
        if (mViewStatus == VIEW_STATUS.EMPTY) return
        // 检查传入的视图的可见性，如果视图的可见性为View.GONE或View.INVISIBLE，则直接返回，不进行任何操作
        if (view.visibility == (View.GONE or View.INVISIBLE)) return
        // 更新空视图的消息
        this.mMessage = msg
        // 获取传入视图的父容器（view.parent）作为ViewGroup
        val viewGroup = view.parent as ViewGroup
        // 从父容器中移除当前的空视图（this）
        viewGroup.removeView(this)
        // 将视图的状态设置为空
        mViewStatus = VIEW_STATUS.EMPTY
        // 通过调用getChildSize(view)方法来获取传入视图的大小
        getChildSize(view)
        // 通过调用setViewAlignAddViewStatus(viewGroup, view)方法将空视图添加到传入视图的父容器中，
        // 并保持相对于传入视图居中对齐的位置
        setViewAlignAddViewStatus(viewGroup, view)
    }

    // 显示加载完成视图
    fun finished(view: View) {
        // 检查当前视图的状态是否为加载完成，如果是的话直接返回，不进行任何操作
        if (mViewStatus == VIEW_STATUS.FINISHED) return
        // 获取传入视图的父容器（view.parent）作为ViewGroup
        val viewGroup = view.parent as ViewGroup
        // 将视图的状态设置为加载完成
        mViewStatus = VIEW_STATUS.FINISHED
        // 使用过渡动画管理器（TransitionManager）开始一个延迟过渡动画
        TransitionManager.beginDelayedTransition(viewGroup)
        // 将传入视图的布局参数（layoutParams）设置为预定义的布局参数（layoutParams）
        view.layoutParams = layoutParams
        // 将传入视图的可见性设置为可见（View.VISIBLE）
        view.visibility = View.VISIBLE
        // 从父容器中移除当前的加载状态视图（this）
        viewGroup.removeView(this)
    }

    //对外提供获取当前View 的状态
    fun getCurrentViewStatus()=mViewStatus


    private fun setViewAlignAddViewStatus(viewGroup: ViewGroup, view: View) {
        // 使用过渡动画管理器（TransitionManager）开始一个延迟过渡动画
        TransitionManager.beginDelayedTransition(viewGroup)
        // 从父容器中移除当前的加载状态视图（this）
        viewGroup.removeView(this)
        // 根据父容器的类型进行不同的操作
        if (viewGroup is ConstraintLayout) {
            // 如果父容器是ConstraintLayout，则将传入视图设置为不可见（View.INVISIBLE）
            view.visibility = View.INVISIBLE
            // 将传入视图的布局参数（layoutParams）转换为ConstraintLayout.LayoutParams，并赋值给加载状态视图的布局参数（layoutParams）
            val contentViewLayoutParams = view.layoutParams as ConstraintLayout.LayoutParams
            layoutParams = contentViewLayoutParams
            // 将加载状态视图添加到父容器中
            viewGroup.addView(this)
        } else if (viewGroup is RelativeLayout) {
            // 如果父容器是RelativeLayout，则将传入视图设置为不可见（View.INVISIBLE）
            view.visibility = View.INVISIBLE
            // 将传入视图的布局参数（layoutParams）转换为RelativeLayout.LayoutParams，并赋值给加载状态视图的布局参数（layoutParams）
            val contentViewLayoutParams = view.layoutParams as RelativeLayout.LayoutParams
            layoutParams = contentViewLayoutParams
            // 将加载状态视图添加到父容器中
            viewGroup.addView(this)
        } else if (viewGroup is LinearLayout) {
            // 如果父容器是LinearLayout，则将传入视图设置为不可见（View.GONE）
            view.visibility = View.GONE
            // 将传入视图的布局参数（layoutParams）转换为LinearLayout.LayoutParams，并赋值给加载状态视图的布局参数（layoutParams）
            val contentViewLayoutParams = view.layoutParams as LinearLayout.LayoutParams
            layoutParams = contentViewLayoutParams
            // 获取传入视图在父容器中的索引位置
            val indexOf = viewGroup.children.indexOf(view)
            // 将加载状态视图添加到父容器的传入视图索引位置处
            viewGroup.addView(this, indexOf)
        } else {
            // 对于其他类型的父容器，默认将传入视图设置为不可见（View.GONE）
            // 将传入视图的布局参数（layoutParams）转换为ViewGroup.LayoutParams，并赋值给加载状态视图的布局参数（layoutParams）
            val contentViewLayoutParams = view.layoutParams as ViewGroup.LayoutParams
            layoutParams = contentViewLayoutParams
            // 将加载状态视图添加到父容器中
            viewGroup.addView(this)
        }
    }

    private fun getChildSize(view: View) {
        // 将当前视图设置为传入的视图
        currentView = view

        // 根据传入的视图类型设置当前视图类型
        mViewType = when (view) {
            is TextView -> VIEW_TYPE.TEXT_VIEW
            else -> VIEW_TYPE.DEFAULT
        }

        // 添加一个全局布局监听器（ViewTreeObserver.OnGlobalLayoutListener）
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 获取子视图的真实宽度和高度
                mWidth = view.width.toFloat()
                mHeight = view.height.toFloat()

                // 如果高度为0，则将高度设置为默认高度（mDefaultHeight）
                if (mHeight.toInt() == 0) {
                    mHeight = mDefaultHeight
                }

                // 如果视图类型为TextView，则将高度设置为-1（表示自适应高度）
                if (mViewType == VIEW_TYPE.TEXT_VIEW) {
                    mHeight = -1f
                }
                // 刷新视图
                refreshViews()
                // 移除全局布局监听器
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun refreshViews() {
        // 根据当前的视图状态执行相应的操作
        when (mViewStatus) {
            VIEW_STATUS.LOADING -> {
                // 添加加载视图
                addLoadingView()
            }
            VIEW_STATUS.ERROR -> {
                // 添加错误视图
                addErrorView()
            }
            VIEW_STATUS.EMPTY -> {
                // 添加空视图
                addEmptyView()
            }
            VIEW_STATUS.FINISHED -> {
                // 如果视图状态为FINISHED，则不执行任何操作
            }
        }
        // 将当前视图根据布局参数和对齐方式添加到父容器中
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
                if(mViewType== VIEW_TYPE.TEXT_VIEW){
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
                text = mMessage
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
                text = mMessage
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
        if (mViewStatus != VIEW_STATUS.FINISHED) {
            canvas.drawRoundRect(
                0f,
                0f,
                width.toFloat() - 0f,
                height.toFloat() - 0f,
                8.dp,
                8.dp,
                mPaint
            )
        }
        super.dispatchDraw(canvas)
    }
}