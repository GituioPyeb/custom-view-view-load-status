package com.example.customviewviewloadstatus

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import com.example.mypototamusic.view.ViewLoadStatus
import com.example.mypototamusic.view.ViewLoadStatusManager


val Float.dp
    get()=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,this,Resources.getSystem().displayMetrics)

val Int.dp
    get() = this.toFloat().dp

val View.showLoadingStatus:ViewLoadStatus
    get() {
        val viewLoadStatusManager = ViewLoadStatusManager.init()
        return viewLoadStatusManager.loading(this)
    }
val View.showErrorStatus:ViewLoadStatus
    get() {
        val viewLoadStatusManager = ViewLoadStatusManager.init()
        return viewLoadStatusManager.error(this)
    }
val View.showEmptyStatus:ViewLoadStatus
    get() {
        val viewLoadStatusManager = ViewLoadStatusManager.init()
        return viewLoadStatusManager.empty(this)
    }
val View.showFinishedStatus:ViewLoadStatus
    get() {
        val viewLoadStatusManager = ViewLoadStatusManager.init()
        return viewLoadStatusManager.finished(this)
    }

fun View.setOnEmptyRetryClickListener(action: (v:View) -> Unit) {
    val viewLoadStatusManager = ViewLoadStatusManager.init()
    viewLoadStatusManager.setOnEmptyRetryClickListener(this,action)
}
fun View.setOnErrorRetryClickListener(action: (v:View) -> Unit) {
    val viewLoadStatusManager = ViewLoadStatusManager.init()
    viewLoadStatusManager.setOnErrorRetryClickListener(this,action)
}

