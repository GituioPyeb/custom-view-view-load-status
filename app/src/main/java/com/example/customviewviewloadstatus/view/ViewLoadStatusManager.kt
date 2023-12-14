package com.example.mypototamusic.view

import android.view.View

class ViewLoadStatusManager {
    private val TAG="ViewLoadStatusManager"
    companion object{
        private var viewStatusManager:ViewLoadStatusManager?=null
        fun init(): ViewLoadStatusManager {
            if(viewStatusManager==null){
                viewStatusManager=ViewLoadStatusManager()
            }
            return viewStatusManager!!
        }
    }
    private val viewLoadStatusMap=HashMap<View,ViewLoadStatus>()
    private val viewLoadStatusErrorClickListenerMap=HashMap<View,(v:View)->Unit>()
    private val viewLoadStatusEmptyClickListenerMap=HashMap<View,(v:View)->Unit>()

    @Synchronized
    fun loading(view: View): ViewLoadStatus {
        var viewLoadStatus = viewLoadStatusMap[view]
        if(viewLoadStatus==null){
            viewLoadStatus= ViewLoadStatus(view.context)
            viewLoadStatusMap[view] = viewLoadStatus
        }
        viewLoadStatus.showViewIsLoading(view)
        return viewLoadStatus
    }
    @Synchronized
    fun error(view: View): ViewLoadStatus {
        var viewLoadStatus = viewLoadStatusMap[view]
        if(viewLoadStatus==null){
            viewLoadStatus= ViewLoadStatus(view.context)
            viewLoadStatusMap[view] = viewLoadStatus
        }
        viewLoadStatus.showViewIsError(view)
        if (viewLoadStatusErrorClickListenerMap[view]!=null) {
            viewLoadStatus.setOnErrorRetryClickListener(viewLoadStatusErrorClickListenerMap[view]!!)
        }
        return viewLoadStatus
    }
    @Synchronized
    fun empty(view: View): ViewLoadStatus {
        var viewLoadStatus = viewLoadStatusMap[view]
        if(viewLoadStatus==null){
            viewLoadStatus= ViewLoadStatus(view.context)
            viewLoadStatusMap[view] = viewLoadStatus
        }
        viewLoadStatus.showViewEmpty(view)
        if (viewLoadStatusEmptyClickListenerMap[view]!=null) {
            viewLoadStatus.setOnEmptyRetryClickListener(viewLoadStatusEmptyClickListenerMap[view]!!)
        }
        return viewLoadStatus
    }
    @Synchronized
    fun finished(view: View): ViewLoadStatus {
        var viewLoadStatus = viewLoadStatusMap[view]
        if(viewLoadStatus==null){
            viewLoadStatus= ViewLoadStatus(view.context)
            viewLoadStatusMap[view] = viewLoadStatus
        }
        viewLoadStatus.finished(view)
        viewLoadStatusMap.remove(view)
        return viewLoadStatus
    }

    fun setOnErrorRetryClickListener(view:View,click:(v:View)->Unit): (View) -> Unit {
        viewLoadStatusErrorClickListenerMap[view]=click
        viewLoadStatusMap[view]?.setOnErrorRetryClickListener(click)
        return click
    }
    fun setOnEmptyRetryClickListener(view: View, click: (v: View) -> Unit): (View) -> Unit {
        viewLoadStatusEmptyClickListenerMap[view] = click
        viewLoadStatusMap[view]?.setOnEmptyRetryClickListener(click)
        return click
    }

    fun getViewLoadStatus(view: View): ViewLoadStatus.VIEW_STATUS? {
        viewLoadStatusMap[view]?.apply {
            return getCurrentViewStatus()
        }
        return null
    }
}