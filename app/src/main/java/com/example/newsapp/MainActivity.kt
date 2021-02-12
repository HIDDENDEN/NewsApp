package com.example.newsapp

//for kotlin synthetic
import android.annotation.SuppressLint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar.*

import android.graphics.Color.alpha
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.newsapp.adapters.RecyclerAdapter
import com.example.newsapp.api.NewsApiJSON
import com.example.newsapp.databinding.ActivityMainBinding
import com.google.android.material.internal.NavigationMenu
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

const val BASE_URL = "https://api.currentsapi.services"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var countDownTimer: CountDownTimer
    private var titleList = mutableListOf<String>()
    private var descList = mutableListOf<String>()
    private var imagesList = mutableListOf<String>()
    private var linksList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)



        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTabBar()
        setSrchBtnListener()
        setUpRecyclerView()
        makeAPIRequest()
        etSearchNews.visibility = View.GONE
        srchBtn.visibility = View.GONE


        bottomNavBar.setItemSelected(R.id.nav_all,true)

//        setSwipeRefreshListener()
    }

    private fun setUpTabBar() {
        binding.bottomNavBar.setOnItemSelectedListener {
            when (it) {
                R.id.nav_all -> {
                    makeAPIRequest()
                    etSearchNews.setText(null)
                    etSearchNews.visibility = View.GONE
                    srchBtn.visibility = View.GONE
                }
                R.id.nav_us -> {
                    makeRegionRequest("US")
                    etSearchNews.visibility = View.GONE
                    srchBtn.visibility = View.GONE
                }
                R.id.nav_fr -> {
                    makeRegionRequest("FR")
                    etSearchNews.visibility = View.GONE
                    srchBtn.visibility = View.GONE
                }

                R.id.nav_srch_enable -> {
                    etSearchNews.visibility = View.VISIBLE
                    srchBtn.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun makeRegionRequest(region: String) {
        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {

            api.getRegionNews("/v1/search?country=" + region + "&language=en&apiKey=gATYOlZGxcSIIXQiryJp1ZRgq6147Wvq3IIDbF2irUfAkpUn")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ newsApiJSON: NewsApiJSON ->
                    run {
//                        Log.i("MainActivity", "Result = ${newsApiJSON.news}")
                        clearLists()
                        for (article in newsApiJSON.news) {
                            addToList(
                                article.title,
                                article.description,
                                article.image,
                                article.url
                            )
                        }
                        //update the UI
                        setUpRecyclerView()
                        fadeInFromBlack()
                        progressBar.visibility = View.GONE
                    }
                }, { throwable ->
                    run {
                        Log.i("MainActivity", "${throwable.toString()}")
                        attemptRequestAgain()
                    }
                });
        }

    }

    private fun setSrchBtnListener() {
        srchBtn.setOnClickListener {
            if (etSearchNews.text.toString().isEmpty())
                Toast.makeText(it.context, "Enter keywords!", Toast.LENGTH_LONG).show()
            else
                makeKeyWordApiRequest(etSearchNews.text.toString())
        }
    }

    private fun makeKeyWordApiRequest(keyword: String) {
        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {

            api.getKeyWordNews("/v1/search?keywords=" + keyword + "&language=en&apiKey=gATYOlZGxcSIIXQiryJp1ZRgq6147Wvq3IIDbF2irUfAkpUn")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ newsApiJSON: NewsApiJSON ->
                    run {
//                        Log.i("MainActivity", "Result = ${newsApiJSON.news}")
                        clearLists()
                        for (article in newsApiJSON.news) {
                            addToList(
                                article.title,
                                article.description,
                                article.image,
                                article.url
                            )
                        }
                        //update the UI
                        setUpRecyclerView()
                        fadeInFromBlack()
                        progressBar.visibility = View.GONE
                    }
                }, { throwable ->
                    run {
                        Log.i("MainActivity", "${throwable.toString()}")
                        attemptRequestAgain()
                    }
                });
        }

    }

    private fun clearLists() {
        titleList.clear()
        descList.clear()
        imagesList.clear()
        linksList.clear()
    }


    private fun fadeInFromBlack() {
        v_blackScreen.animate().apply {
            alpha(0f)
            duration = 3000
        }.start()
    }

    private fun setUpRecyclerView() {

        rv_recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        rv_recyclerView.adapter = RecyclerAdapter(titleList, descList, imagesList, linksList)
    }

    private fun addToList(title: String, desc: String, image: String, link: String) {
        titleList.add(title)
        descList.add(desc)
        imagesList.add(image)
        linksList.add(link)
    }

    @SuppressLint("CheckResult")
    private fun makeAPIRequest() {
        clearLists()
        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {

            api.getNews()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ newsApiJSON: NewsApiJSON ->
                    run {
                        Log.i("MainActivity", "Result = ${newsApiJSON.news}")
                        for (article in newsApiJSON.news) {
                            addToList(
                                article.title,
                                article.description,
                                article.image,
                                article.url
                            )
                        }
                        //update the UI
                        setUpRecyclerView()
                        fadeInFromBlack()
                        progressBar.visibility = View.GONE
                    }
                }, { throwable ->
                    run {
                        Log.i("MainActivity", "${throwable.toString()}")
                        attemptRequestAgain()
                    }
                });
        }

//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val response = api.getNews() //get method from our Interface
//
//                for (article in response.news) {
//                    Log.i("MainActivity", "Result = ${article}")
//                    addToList(article.title, article.description, article.image, article.url)
//                }
//
//                //update the UI
//                withContext(Dispatchers.Main) {
//                    setUpRecyclerView()
//                    fadeInFromBlack()
//                    progressBar.visibility = View.GONE
//                }
//            } catch (e: Exception) {
//                Log.i("MainActivity", "${e.toString()}")
//
//                withContext(Dispatchers.Main) {
//                    attemptRequestAgain()
//                }
//            }
//        }
    }

    private fun attemptRequestAgain() {
        countDownTimer = object : CountDownTimer(5 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                makeAPIRequest()
                countDownTimer.cancel()
            }

            override fun onFinish() {

                Log.i("MainActivity", "Couldn't get data... Trying again in ${5000 / 1000} seconds")
            }

        }

        countDownTimer.start()
    }


//    private fun setSwipeRefreshListener() {
//        swipeRefreshLayout.setOnRefreshListener {
//            v_blackScreen.visibility=View.VISIBLE
//            makeAPIRequest()
//            swipeRefreshLayout.isRefreshing = false
//        }
//    }
}