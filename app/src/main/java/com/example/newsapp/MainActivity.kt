package com.example.newsapp


import android.annotation.SuppressLint
import android.content.res.Resources
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar.*

import android.graphics.Color.alpha
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
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

        //for bottom navigation bar
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setting gradient color for top toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.myToolBar)
        toolbar.setBackground(getDrawable(R.drawable.toolbar_gradient))
        setSupportActionBar(toolbar)

        val bottomToolBar =
            findViewById<com.ismaeldivita.chipnavigation.ChipNavigationBar>(R.id.bottomNavBar)
        bottomToolBar.setBackground(getDrawable(R.drawable.bottom_toolbar_gradient))
        setSupportActionBar(toolbar)

        //setting up onClick reaction for switching regions
        setUpTabBar()

        setUpRecyclerView()

        makeAPIRequest()

        bottomNavBar.setItemSelected(R.id.nav_all, true)
    }

    private fun setUpTabBar() {
        binding.bottomNavBar.setOnItemSelectedListener {
            when (it) {
                R.id.nav_all -> {
                    makeAPIRequest()
                }
                R.id.nav_us -> {
                    makeRegionRequest("US")
                }
                R.id.nav_fr -> {
                    makeRegionRequest("FR")
                }


            }
        }
    }

    private fun makeRegionRequest(region: String) {
        fadeInToBlack()
        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {

            api.getRegionNews("/v1/search?country=" + region + "&language=en&apiKey=" + getString(R.string.apiKey))
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

    private fun makeKeyWordApiRequest(keyword: String) {
        fadeInToBlack()
        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {

            api.getKeyWordNews("/v1/search?keywords=" + keyword + "&language=en&apiKey=" + getString(R.string.apiKey))
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

    private fun fadeInToBlack() {
        v_blackScreen.animate().apply {
            alpha(1f)
            duration = 500
        }
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
        fadeInToBlack()
        clearLists()
        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {

            api.getNews("/v1/latest-news?language=en&apiKey=" + getString(R.string.apiKey))
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
    }

    private fun attemptRequestAgain() {
        countDownTimer = object : CountDownTimer(5 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTimer.cancel()
            }

            override fun onFinish() {
                makeAPIRequest()
                countDownTimer.cancel()
                Log.i("MainActivity", "Couldn't get data... Trying again in ${5000 / 1000} seconds")
            }

        }

        countDownTimer.start()
    }

    //top nav bar menu configuring
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.nav_srch_menu, menu)
        val search = menu?.findItem(R.id.nav_search)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = "Search something!"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {

                if (query.toString().isEmpty()) {
                    Toast.makeText(applicationContext, "Enter keywords!", Toast.LENGTH_LONG).show()
                    return true
                } else {
                    makeKeyWordApiRequest(query.toString())
                    return true
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }
}


