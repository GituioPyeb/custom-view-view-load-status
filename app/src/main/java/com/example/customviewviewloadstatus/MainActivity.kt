package com.example.customviewviewloadstatus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customviewviewloadstatus.adapter.TestContentAdapter
import com.example.customviewviewloadstatus.databinding.ActivityMainBinding
import com.example.customviewviewloadstatus.view.showEmptyStatus
import com.example.customviewviewloadstatus.view.showErrorStatus
import com.example.customviewviewloadstatus.view.showFinishedStatus
import com.example.customviewviewloadstatus.view.showLoadingStatus
import com.example.mypototamusic.view.ViewLoadStatus
import com.example.mypototamusic.view.ViewLoadStatusManager
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    private val contentAdapter by lazy {
        TestContentAdapter()
    }
    private val dataList = List(100) { index -> "Item ${index + 1}" }
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.cardView.showErrorStatus
        ViewLoadStatusManager.getInstance().setOnErrorRetryClickListener(binding.cardView){
            Toast.makeText(this@MainActivity, "错误时", Toast.LENGTH_SHORT).show()
        }
        ViewLoadStatusManager.getInstance().setOnEmptyRetryClickListener(binding.cardView){
            Toast.makeText(this@MainActivity, "空状态时", Toast.LENGTH_SHORT).show()
        }
//        binding.cardView.setOnErrorRetryClickListener{
//            Toast.makeText(this@MainActivity, "错误时", Toast.LENGTH_SHORT).show()
//        }
//        binding.cardView.setOnEmptyRetryClickListener{
//            Toast.makeText(this@MainActivity, "空状态时", Toast.LENGTH_SHORT).show()
//        }

//        val statusManager = ViewLoadStatusManager.init()
//        statusManager.empty(binding.cardView)
//        statusManager.setOnEmptyRetryClickListener(binding.cardView){
//            Toast.makeText(this@MainActivity, "重试", Toast.LENGTH_SHORT).show()
//        }

//        ViewLoadStatus(this).apply {
//            showViewIsError(binding.cardView)
//            setOnErrorRetryClickListener {
//                Toast.makeText(this@MainActivity, "重试", Toast.LENGTH_SHORT).show()
//            }
//        }
        
//        binding.image1.setOnErrorRetryClickListener {
//            Toast.makeText(this, "图片错误时", Toast.LENGTH_SHORT).show()
//        }
//        binding.image1.setOnEmptyRetryClickListener {
//            Toast.makeText(this, "图片为空时", Toast.LENGTH_SHORT).show()
//        }

        binding.changeViewLoadStatusBtn.setOnClickListener {
            when(ViewLoadStatusManager.getInstance().getViewLoadStatus(binding.image1)){
                ViewLoadStatus.VIEW_STATUS.LOADING -> {
                    binding.image1.showErrorStatus
                    binding.cardView.showErrorStatus
                    binding.testTextView.showErrorStatus
                    binding.contentRecyclerView.showErrorStatus
                }
                ViewLoadStatus.VIEW_STATUS.ERROR -> {
                    binding.image1.showEmptyStatus
                    binding.cardView.showEmptyStatus.setOnErrorRetryClickListener {
                        Toast.makeText(this, "空状态", Toast.LENGTH_SHORT).show()
                    }
                    binding.contentRecyclerView.showEmptyStatus
                    binding.testTextView.showEmptyStatus
                }
                ViewLoadStatus.VIEW_STATUS.EMPTY -> {
                    binding.image1.showFinishedStatus
                    binding.cardView.showFinishedStatus
                    binding.contentRecyclerView.showFinishedStatus
                    binding.testTextView.showFinishedStatus
                }
                ViewLoadStatus.VIEW_STATUS.FINISHED -> {
                    binding.image1.showLoadingStatus
                    binding.cardView.showLoadingStatus
                    binding.testTextView.showLoadingStatus
                    binding.contentRecyclerView.showLoadingStatus
                }
                null -> {
                    binding.image1.showLoadingStatus
                    binding.cardView.showLoadingStatus
                    binding.contentRecyclerView.showLoadingStatus
                    binding.testTextView.showLoadingStatus
                }
            }
        }

        binding.contentRecyclerView.apply {
            layoutManager=LinearLayoutManager(context,LinearLayoutManager.VERTICAL,true)
            adapter=contentAdapter
        }

        Timer().schedule(object:TimerTask(){
            override fun run() {
                runOnUiThread {
                    contentAdapter.submitList(dataList)
//                    binding.contentRecyclerView.showFinishedStatus
                }
            }
        },1000)





    }

    override fun onDestroy() {
        super.onDestroy()
        ViewLoadStatusManager.getInstance().unbindViews(this)
    }
}

