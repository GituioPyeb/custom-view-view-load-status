package com.example.viewloadstatuslibs.view

import android.view.View
import android.view.ViewGroup


val View.showLoadingStatus: ViewLoadStatus
    get() {
        val viewLoadStatusManager = ViewLoadStatusManager.getInstance()
        return viewLoadStatusManager.loading(this)
    }
val View.showErrorStatus: ViewLoadStatus
    get() {
        val viewLoadStatusManager = ViewLoadStatusManager.getInstance()
        return viewLoadStatusManager.error(this)
    }
val View.showEmptyStatus: ViewLoadStatus
    get() {
        val viewLoadStatusManager = ViewLoadStatusManager.getInstance()
        return viewLoadStatusManager.empty(this)
    }
val View.showFinishedStatus: ViewLoadStatus
    get() {
        val viewLoadStatusManager = ViewLoadStatusManager.getInstance()
        return viewLoadStatusManager.finished(this)
    }
val ViewGroup.unbindAllViewStatus:Boolean
    get() {
        val viewLoadStatusManager = ViewLoadStatusManager.getInstance()
        return viewLoadStatusManager.unbindViews(this)
    }

fun View.setOnEmptyRetryClickListener(action: (v:View) -> Unit) {
    val viewLoadStatusManager = ViewLoadStatusManager.getInstance()
    viewLoadStatusManager.setOnEmptyRetryClickListener(this,action)
}
fun View.setOnErrorRetryClickListener(action: (v:View) -> Unit) {
    val viewLoadStatusManager = ViewLoadStatusManager.getInstance()
    viewLoadStatusManager.setOnErrorRetryClickListener(this,action)
}

