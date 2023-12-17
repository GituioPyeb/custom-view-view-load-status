package com.example.viewloadstatuslibs.view

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children

class ViewLoadStatusManager private constructor(){
    companion object {
        @Volatile
        private var instance: ViewLoadStatusManager? = null
        // 获取或初始化 ViewLoadStatusManager 的单例实例
        fun getInstance(): ViewLoadStatusManager {
            return instance ?: synchronized(this) {
                instance ?: ViewLoadStatusManager().also { instance = it }
            }
        }
    }

    private val viewLoadStatusMap = LinkedHashMap<View, ViewLoadStatus>() // 存储视图和对应的 ViewLoadStatus 实例的映射关系

    @Synchronized
    fun loading(view: View): ViewLoadStatus {
        val viewLoadStatus = viewLoadStatusMap[view] ?: createAndCacheStatus(view)
        viewLoadStatus.showViewIsLoading(view) // 展示加载状态
        return viewLoadStatus
    }

    @Synchronized
    fun error(view: View): ViewLoadStatus {
        val viewLoadStatus = viewLoadStatusMap[view] ?: createAndCacheStatus(view)
        viewLoadStatus.showViewIsError(view) // 展示错误状态
        return viewLoadStatus
    }

    @Synchronized
    fun empty(view: View): ViewLoadStatus {
        val viewLoadStatus = viewLoadStatusMap[view] ?: createAndCacheStatus(view)
        viewLoadStatus.showViewEmpty(view) // 展示空状态
        return viewLoadStatus
    }

    @Synchronized
    fun finished(view: View): ViewLoadStatus {
        val viewLoadStatus = viewLoadStatusMap[view] ?: createAndCacheStatus(view) // 获取视图对应的 ViewLoadStatus 实例
        viewLoadStatus.finished(view) // 标记加载完成状态
        return viewLoadStatus
    }
    @Synchronized
    fun setViewShowStatus(view: View, viewStatus: ViewLoadStatus.VIEW_STATUS){
        val viewLoadStatus = viewLoadStatusMap[view] ?: createAndCacheStatus(view)
        viewLoadStatus.setViewLoadStatus(viewStatus,view)
    }

    // 设置错误点击监听器
    fun setOnErrorRetryClickListener(view: View, click: (v: View) -> Unit): (View) -> Unit {
        val viewLoadStatus = viewLoadStatusMap[view] ?: createAndCacheStatus(view)
        viewLoadStatus.setOnErrorRetryClickListener(click)
        return click
    }

    // 设置空点击监听器
    fun setOnEmptyRetryClickListener(view: View, click: (v: View) -> Unit): (View) -> Unit {
        val viewLoadStatus = viewLoadStatusMap[view] ?: createAndCacheStatus(view)
        viewLoadStatus.setOnEmptyRetryClickListener(click)
        return click
    }

    // 获取视图的加载状态
    fun getViewShowStatus(view: View): ViewLoadStatus.VIEW_STATUS? {
        viewLoadStatusMap[view]?.let {
            return it.getCurrentViewStatus()
        }
        return null
    }

    private fun createAndCacheStatus(view: View): ViewLoadStatus {
        val viewLoadStatus = ViewLoadStatus(view.context)
        viewLoadStatusMap[view] = viewLoadStatus
        return viewLoadStatus
    }

    fun unbindViews(activity: Activity) {
        val rootViewGroup =
            activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        unbindViewStatus(rootViewGroup)
    }
    fun unbindViews(viewGroup: ViewGroup): Boolean {
        unbindViewStatus(viewGroup)
        return true
    }
    private fun unbindViewStatus(rootViewGroup: ViewGroup) {
        for (child in rootViewGroup.children) {
            if(child is ViewGroup){
                unbindViewStatus(child)
            }else{
                if (viewLoadStatusMap.contains(child)) {
                    viewLoadStatusMap.remove(child)
                }
            }
        }
    }
}